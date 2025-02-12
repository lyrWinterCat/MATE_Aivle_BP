function captureShareScreen(){
    const video = document.getElementById('localVideo');
    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');

    if (video.videoWidth === 0 || video.videoHeight === 0) return;

    canvas.width = video.videoWidth;
    canvas.height = video.videoHeight;
    context.drawImage(video, 0, 0, canvas.width, canvas.height);

    canvas.toBlob(async (blob) => {
        const sanitizedFilename = sanitizeFilename(`screenshot-${new Date().toISOString()}.png`);
        const meetingName = document.querySelector("#meetingTitle p").textContent.split(" : ")[1]

        const formData = new FormData();
        formData.append('image', blob, sanitizedFilename);
        formData.append('meeting_name', meetingName);

        try{
            const response = await fetch('https://mate-fastapi-hxaybhbzakhvfvhf.koreacentral-01.azurewebsites.net/summarize_screen', {
                method: 'POST',
                body: formData
            });

            if (response.ok){
                const data = await response.text();
                if (data){
                    const screenSummaryBox = document.getElementById("screenSummary");
                    const screenSummaryContent = document.getElementById("screenSummaryContent");

                    screenSummaryBox.style.display = "flex";
                    screenSummaryContent.innerHTML = JSON.parse(data);

                }
            }
        } catch (error) {
            console.error("이미지 전송 중 오류:", error);
        }
    }, 'image/png');
}

function closeScreenSummary(){
    const screenSummaryBox = document.getElementById("screenSummary");
    screenSummaryBox.style.display = "none";
}

document.addEventListener('DOMContentLoaded', function(){
    document.getElementById("screenSummaryButton").addEventListener("click", captureShareScreen);
    document.getElementById("screenSummaryCloseButton").addEventListener("click", closeScreenSummary);
});