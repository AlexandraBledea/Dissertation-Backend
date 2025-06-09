import requests

from contants import BENCHMARK_SERVICE_URL


def fetch_experiment_csv(experiment_id: int) -> str:
    response = requests.get(f"{BENCHMARK_SERVICE_URL}/csv/{experiment_id}")
    response.raise_for_status()
    return response.text

def fetch_filtered_experiments_with_csv(broker: str = None, message_count: int = None, message_size_kb: int = None):
    params = {}
    if broker:
        params["broker"] = broker
    if message_count:
        params["count"] = str(message_count)
    if message_size_kb:
        params["sizeKb"] = str(message_size_kb)

    response = requests.get(f"{BENCHMARK_SERVICE_URL}/filter", params=params)
    response.raise_for_status()
    return response.json()
