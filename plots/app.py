import base64

from fastapi import FastAPI, HTTPException, Query
from fastapi.responses import StreamingResponse
from starlette.middleware.cors import CORSMiddleware
from starlette.responses import JSONResponse

from benchmark_client import fetch_experiment_csv, fetch_filtered_experiments_with_csv
from contants import METRIC_MAP, TREND_METRIC_MAP
from services import (generate_plot_for_one_experiment_over_time, generate_weighted_latency_vs_size_plot,
                      generate_metric_vs_size_plot, generate_energy_per_message_vs_size_plot,
                      generate_bar_plot_across_brokers)
import logging

app = FastAPI()

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_methods=["*"],
    allow_headers=["*"],
    allow_credentials=False
)

logging.basicConfig(level=logging.INFO)
logger = logging.getLogger(__name__)

@app.get("/plot/full/{experiment_id}")
def get_plot_for_one_experiment_over_time(experiment_id: int):
    try:
        csv_data = fetch_experiment_csv(experiment_id)
    except Exception:
        raise HTTPException(status_code=500, detail="Could not fetch experiment data")

    plot_image = generate_plot_for_one_experiment_over_time(csv_data)
    return StreamingResponse(plot_image, media_type="image/png")


@app.get("/plot/trend/latency-vs-size")
def get_latency_vs_size_plot(broker: str, message_count: int = Query(None)):
    try:
        experiments = fetch_filtered_experiments_with_csv(broker, message_count)
    except Exception:
        raise HTTPException(status_code=500, detail="Could not fetch experiments")

    if not experiments:
        raise HTTPException(status_code=404, detail="No matching experiments found")

    plot = generate_weighted_latency_vs_size_plot(experiments)
    plot.seek(0)
    encoded = base64.b64encode(plot.read()).decode("utf-8")
    data_url = f"data:image/png;base64,{encoded}"
    return JSONResponse(content={"image": data_url})



@app.get("/plot/trend/energy-per-message-vs-size")
def get_latency_vs_size_plot(broker: str, message_count: int = Query(None)):
    try:
        experiments = fetch_filtered_experiments_with_csv(broker, message_count)
    except Exception:
        raise HTTPException(status_code=500, detail="Could not fetch experiments")

    if not experiments:
        raise HTTPException(status_code=404, detail="No matching experiments found")

    plot = generate_energy_per_message_vs_size_plot(experiments)

    plot.seek(0)
    encoded = base64.b64encode(plot.read()).decode("utf-8")
    data_url = f"data:image/png;base64,{encoded}"
    return JSONResponse(content={"image": data_url})


@app.get("/plot/trend/metric-vs-size")
def get_trend_plot(
    broker: str = Query(..., description="Broker name (e.g., kafka, redis, rabbitmq)"),
    metric: str = Query(..., description="Metric: throughput, cpu, memory, energy"),
    message_count: int = Query(None, description="Optional filter by number of messages")
):
    # Validate metric
    if metric not in TREND_METRIC_MAP:
        raise HTTPException(status_code=400, detail="Invalid metric. Choose from: throughput, cpu, memory, energy")

    try:
        experiments = fetch_filtered_experiments_with_csv(broker, message_count)
    except Exception:
        raise HTTPException(status_code=500, detail="Could not fetch experiments")

    if not experiments:
        raise HTTPException(status_code=404, detail="No matching experiments found")

    config = TREND_METRIC_MAP[metric]

    plot = generate_metric_vs_size_plot(
        experiments,
        metric_name=config["field"],
        y_label=config["y_label"],
        title=config["title"]
    )

    plot.seek(0)
    encoded = base64.b64encode(plot.read()).decode("utf-8")
    data_url = f"data:image/png;base64,{encoded}"
    return JSONResponse(content={"image": data_url})


@app.get("/plot/comparison-bar")
def get_comparison_bar_plot(
    metric: str = Query(..., description="Metric to plot (latency, throughput, cpu, memory, energy)"),
    message_size: int = Query(..., description="Message size in KB"),
    message_count: int = Query(..., description="Number of messages (optional)")
):

    if metric not in METRIC_MAP:
        raise HTTPException(status_code=400, detail="Invalid metric. Choose from: latency, throughput, cpu, memory, energy")

    try:
        experiments = fetch_filtered_experiments_with_csv(
            broker=None,
            message_count=message_count,
            message_size_kb=message_size
        )
    except Exception:
        raise HTTPException(status_code=500, detail="Could not fetch experiments")

    if not experiments:
        raise HTTPException(status_code=404, detail="No matching experiments found")

    metric_name = METRIC_MAP[metric]

    plot = generate_bar_plot_across_brokers(
        experiments,
        metric_name=metric_name,
        message_size_kb=message_size,
        message_count=message_count
    )
    plot.seek(0)
    encoded = base64.b64encode(plot.read()).decode("utf-8")
    data_url = f"data:image/png;base64,{encoded}"
    return JSONResponse(content={"image": data_url})