from collections import defaultdict

import matplotlib.pyplot as plt
import pandas as pd
import io

from contants import EXPECTED_BROKERS, BROKER_COLORS
from utils import save_plot_to_bytesio, plot_not_enough_data_message, annotate_points, plot_metric


def generate_plot_for_one_experiment_over_time(csv_text: str) -> io.BytesIO:
    df = pd.read_csv(io.StringIO(csv_text))
    df["timestamp"] = pd.to_datetime(df["timestamp"])
    df = df[df["throughput_mps"] > 0]

    if len(df) < 2:
        return save_plot_to_bytesio(plot_not_enough_data_message())

    df["time_seconds"] = (df["timestamp"] - df["timestamp"].iloc[0]).dt.total_seconds()
    fig, axs = plt.subplots(2, 2, figsize=(14, 10), constrained_layout=True)
    fig.suptitle("Metrics Overview Over Time", fontsize=16, y=1.03)

    plot_metric(axs[0, 0], df["time_seconds"], df["latency_ms"], "Latency (ms)", "crimson", "o", "Latency", offset=0.5)
    plot_metric(axs[0, 1], df["time_seconds"], df["cpu_percent"], "CPU Usage (%)", "green", ".", "CPU %", offset=0.5)
    plot_metric(axs[1, 0], df["time_seconds"], df["mem_used_mb"], "Memory Usage (MB)", "pink", "x", "Memory", offset=0.5)
    plot_metric(axs[1, 1], df["time_seconds"], df["throughput_mps"], "Throughput (msg/s)", "orange", "^", "Throughput", offset=0.5)

    return save_plot_to_bytesio(fig)



def generate_weighted_latency_vs_size_plot(experiments):
    grouped = defaultdict(list)

    for exp in experiments:
        try:
            size_kb = exp["messageSizeInKB"]
            latency = exp["latency"]
            number_of_messages = exp["numberOfMessages"]

            if latency is not None and number_of_messages > 0:
                grouped[size_kb].append((latency, number_of_messages))
        except KeyError:
            continue

    if len(grouped) < 2:
        return save_plot_to_bytesio(plot_not_enough_data_message())

    sizes, avg_values = [], []
    for size_kb in sorted(grouped):
        values = grouped[size_kb]
        weighted_sum = sum(value * count for value, count in values)
        total_messages = sum(count for _, count in values)
        if total_messages > 0:
            sizes.append(size_kb)
            avg_values.append(weighted_sum / total_messages)

    # Plot
    fig, ax = plt.subplots(figsize=(8, 5))
    ax.plot(sizes, avg_values, marker='o', linestyle='-', color='crimson')
    ax.set_title("Latency vs Message Size")
    ax.set_xlabel("Message Size (KB)")
    ax.set_ylabel("Latency (ms)")
    ax.grid(True)
    annotate_points(ax, sizes, avg_values)
    plt.tight_layout()

    return save_plot_to_bytesio(fig)



def generate_metric_vs_size_plot(experiments, metric_name, y_label=None, title=None):
    grouped = defaultdict(list)

    for exp in experiments:
        try:
            size_kb = exp["messageSizeInKB"]
            value = exp[metric_name]

            if value is not None:
                grouped[size_kb].append(value)
        except KeyError:
            continue

    if len(grouped) < 2:
        return save_plot_to_bytesio(plot_not_enough_data_message())

    sizes, avg_values = [], []
    for size_kb in sorted(grouped):
        values = grouped[size_kb]
        sizes.append(size_kb)
        avg_values.append(sum(values)/len(values))

    # Plot
    fig, ax = plt.subplots(figsize=(8, 5))
    ax.plot(sizes, avg_values, marker='o', linestyle='-', color='steelblue')
    ax.set_title(title or f"{metric_name} vs Message Size")
    ax.set_xlabel("Message Size (KB)")
    ax.set_ylabel(y_label or f"Average {metric_name}")
    ax.grid(True)
    annotate_points(ax, sizes, avg_values)
    plt.tight_layout()

    return save_plot_to_bytesio(fig)



def generate_energy_per_message_vs_size_plot(experiments):
    grouped = defaultdict(list)

    for exp in experiments:
        try:
            size_kb = exp["messageSizeInKB"]
            energy = exp["energy"]
            number_of_messages = exp["numberOfMessages"]

            if energy is not None and number_of_messages > 0:
                grouped[size_kb].append((energy, number_of_messages))

        except KeyError:
            continue

    if len(grouped) < 2:
        return save_plot_to_bytesio(plot_not_enough_data_message())


    sizes, avg_energy = [], []
    for size_kb in sorted(grouped):
        values = grouped[size_kb]
        total_energy = sum(e for e, _ in values)
        total_msgs = sum(n for _, n in values)
        if total_msgs > 0:
            sizes.append(size_kb)
            avg_energy.append(total_energy / total_msgs)

    fig, ax = plt.subplots(figsize=(8, 5))
    ax.plot(sizes, avg_energy, marker='o', linestyle='-', color='crimson')
    ax.set_title("Energy Consumption per Message vs Message Size")
    ax.set_xlabel("Message Size (KB)")
    ax.set_ylabel("Energy per Message (J)")
    ax.grid(True)
    annotate_points(ax, sizes, avg_energy)
    plt.tight_layout()

    return save_plot_to_bytesio(fig)



def generate_bar_plot_across_brokers(experiments, metric_name, message_size_kb=None, message_count=None):
    grouped = defaultdict(list)

    # Step 1: Group values per broker
    for exp in experiments:
        broker = exp["broker"]
        value = exp[metric_name]
        if broker and value is not None:
            grouped[broker].append(value)

    # Step 2: Ensure all brokers are present
    if not all(broker in grouped and grouped[broker] for broker in EXPECTED_BROKERS):
        return save_plot_to_bytesio(plot_not_enough_data_message())

    # Step 3: Compute averages
    brokers = EXPECTED_BROKERS
    avg_values = [sum(grouped[b]) / len(grouped[b]) for b in brokers]
    colors = [BROKER_COLORS[b] for b in brokers]

    # Step 4: Prepare title
    title_parts = [f"{metric_name.capitalize()} Comparison"]
    if message_size_kb is not None:
        title_parts.append(f"{message_size_kb}KB")
    if message_count is not None:
        title_parts.append(f"{message_count} messages")

    # Step 5: Plot
    fig, ax = plt.subplots(figsize=(8, 5))
    ax.bar(brokers, avg_values, color=colors)
    ax.set_title(" - ".join(title_parts))
    ax.set_xlabel("Broker")
    ax.set_ylabel({
        "latency": "Latency (ms)",
        "throughput": "Throughput (msg/s)",
        "averageCpu": "CPU Usage (%)",
        "averageMemory": "Memory Usage (MB)",
        "energy": "Energy (J)"
    }.get(metric_name, metric_name))

    # Step 6: Extend y-axis limit for safe label placement
    y_max = max(avg_values)
    ax.set_ylim(0, y_max * 1.15)  # add 15% headroom

    # Annotate values above bars with better offset
    for i, broker in enumerate(brokers):
        avg = avg_values[i]
        count = len(grouped[broker])
        ax.text(
            i,
            avg + y_max * 0.03,
            f"{avg:.2f}\n{count} experiment(s)",
            ha='center',
            va='bottom',
            fontsize=9
        )

    if metric_name == "latency":
        ax.set_yscale("log")
    ax.grid(False)
    plt.subplots_adjust(top=0.88)
    plt.tight_layout()

    return save_plot_to_bytesio(fig)