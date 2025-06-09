import io
from matplotlib import pyplot as plt


def plot_not_enough_data_message() -> plt.Figure:
    fig, ax = plt.subplots(figsize=(8, 4))
    ax.text(0.5, 0.5, "Not enough data to generate plot",
            horizontalalignment='center',
            verticalalignment='center',
            fontsize=14, color='gray')
    plt.axis("off")
    return fig

def plot_metric(ax, x, y, title, color, marker, ylabel, offset=0):
    ax.plot(x, y, color=color, marker=marker)
    for xi, yi in zip(x, y):
        ax.text(
            xi,
            yi + offset,
            f"{yi:.1f}",
            fontsize=8,
            fontweight='bold',
            ha='center',
            va='bottom'
        )
    ax.set_title(title)
    ax.set_xlabel("Time (s)")
    ax.set_ylabel(ylabel)
    ax.grid(True)

def save_plot_to_bytesio(fig: plt.Figure) -> io.BytesIO:
    buf = io.BytesIO()
    fig.savefig(buf, format="png")
    plt.close(fig)
    buf.seek(0)
    return buf

def annotate_points(ax, x_vals, y_vals):
    for x, y in zip(x_vals, y_vals):
        ax.annotate(f"{y:.4f}", (x, y), textcoords="offset points", xytext=(0, 5), ha='center', fontsize=8)

def weighted_average(pairs: list[tuple[float, int]]) -> float:
    weighted_sum = sum(v * w for v, w in pairs)
    total_weight = sum(w for _, w in pairs)
    return weighted_sum / total_weight if total_weight > 0 else 0