from pyannote.database import registry, FileFinder
from pyannote.audio import Pipeline
from pyannote.metrics.diarization import DiarizationErrorRate

from fine_tuning import fine_tune_model
import time

with open("huggingFace_token.txt", "r") as f:
    hg_token = f.readline()

registry.load_database("AMI-diarization-setup/pyannote/database.yml")
dataset = registry.get_protocol("meeting.SpeakerDiarization.test", {'audio': FileFinder()})

pretrained_pipeline = Pipeline.from_pretrained("pyannote/speaker-diarization-3.1", use_auth_token=hg_token)

pretrained_metric = DiarizationErrorRate()

validations = []

for file in dataset.test():
    # apply pretrained pipeline
    file["pretrained pipeline"] = pretrained_pipeline(file)

    # evaluate its performance
    pretrained_metric(file["annotation"], file["pretrained pipeline"], uem=file["annotated"])

    pretrained = {
        "model": "pretrained",
        "DER": 100 * abs(pretrained_metric),
        "cunfusion": 100 * pretrained_metric.accumulated_["confusion"]/pretrained_metric.accumulated_["total"],
        "false alarm": 100 * pretrained_metric.accumulated_["false alarm"]/pretrained_metric.accumulated_["total"],
        "missed detection": 100 * pretrained_metric.accumulated_["missed detection"]/pretrained_metric.accumulated_["total"]
    }

    validations.append(pretrained)

print(f"The pretrained pipeline reaches a Diarization Error Rate (DER) of {100 * abs(pretrained_metric):.1f}% on {dataset.name} test set.")
     
with open("fine_tuning_result.txt", 'w') as f:
    for idx in range(10):
        start = time.time()
        best_segmentation_threshold, best_clustering_threshold, temp_metric = fine_tune_model(pretrained_pipeline, dataset, hg_token)
        missed = temp_metric.accumulated_["confusion"]/temp_metric.accumulated_["total"]
        print(f"The finetuned pipeline reaches a Diarization Error Rate (DER) of {100 * abs(temp_metric):.1f}% on {idx}.")
        text = f"index: {idx}, segmentation: {best_segmentation_threshold}, clustering: {best_clustering_threshold}, DER: {100 * abs(temp_metric):.1f}%, missed: {100 * missed:.1f}%\n"
        print(text)
        print(idx, time.time() - start)
        f.write(text)

        temp = {
            "model": f"fine_tuned_{idx}",
            "DER": 100 * abs(temp_metric),
            "cunfusion": 100 * temp_metric.accumulated_["confusion"]/temp_metric.accumulated_["total"],
            "false alarm": 100 * temp_metric.accumulated_["false alarm"]/temp_metric.accumulated_["total"],
            "missed detection": 100 * temp_metric.accumulated_["missed detection"]/temp_metric.accumulated_["total"]
        }

        validations.append(temp)

print("end")
        

        

