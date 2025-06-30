
BENCHMARK_SERVICE_URL = "http://localhost:8080/api/experiment"

METRIC_MAP = {
    "latency": "latency",
    "throughput": "throughput",
    "cpu": "averageCpu",
    "memory": "averageMemory",
    "energy": "energy"
}

TREND_METRIC_MAP = {
    "throughput": {
        "field": "throughput",
        "y_label": "Throughput (messages/sec)",
        "title": "Throughput vs Message Size"
    },
    "cpu": {
        "field": "averageCpu",
        "y_label": "CPU Usage (%)",
        "title": "CPU Usage vs Message Size"
    },
    "memory": {
        "field": "averageMemory",
        "y_label": "Memory Usage (MB)",
        "title": "Memory Usage vs Message Size"
    },
    "energy": {
        "field": "energy",
        "y_label": "Energy Consumption (J)",
        "title": "Energy Consumption vs Message Size"
    }
}

EXPECTED_BROKERS = ["kafka", "rabbitmq", "redis"]

BROKER_COLORS = {"kafka": "#FFCB47", "rabbitmq": "#C589E8", "redis": "#0F7173"}
