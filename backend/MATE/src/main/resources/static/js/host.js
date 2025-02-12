let localStream = null;
let mediaRecorder;
let audioChunks = [];
let recordingInterval;
let captureInterval;
let isCaptureStopping = false;

// 화면 및 오디오 스트림 시작 함수
async function startCapture() {
    try {
        localStream = await navigator.mediaDevices.getDisplayMedia({
            video: true,
            audio: true
        });
        document.getElementById('localVideo').srcObject = localStream;

        // 화면 캡처 및 오디오 녹음 시작
        startAudioRecording();
        startScreenCapture();
    } catch (error) {
        console.error('미디어 캡처 중 오류 발생:', error);
    }
}

function sanitizeFilename(filename) {
    // 유효하지 않은 문자를 '_'로 대체
    return filename.replace(/[<>:"/\\|?*]/g, '_');
}

// 화면 캡처 시작
function startScreenCapture() {
    const video = document.getElementById('localVideo');
    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');

    captureInterval = setInterval(() => {
        if (video.videoWidth === 0 || video.videoHeight === 0) return;

        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        context.drawImage(video, 0, 0, canvas.width, canvas.height);

        // 캡쳐된 이미지를 서버로 전송
        canvas.toBlob(async (blob) => {
            const sanitizedFilename = sanitizeFilename(`screenshot-${new Date().toISOString()}.png`);
            const formData = new FormData();
            formData.append('image', blob, sanitizedFilename);

            try {
                const response = await fetch('https://mate-fastapi-hxaybhbzakhvfvhf.koreacentral-01.azurewebsites.net/detect_fatigue', {
                    method: 'POST',
                    body: formData
                });

                if (response.ok) {
                    const data = await response.json()

                    if (data.image){
                        const imageElement = document.getElementById("imageDisplay");
                        imageElement.src = `data:image/png;base64,${data.image}`;
                        imageElement.style.display = "block";

                        setTimeout(() => {
                            imageElement.style.display = "none";
                        }, 5000);
                    }

                    console.log("이미지 업로드 성공");

                } else {
                    console.error('이미지 업로드 실패');
                }
            } catch (error) {
                console.error('이미지 전송 중 오류:', error);
            }
        }, 'image/png');
    }, 15000);
}

// 오디오 녹음 시작
function startAudioRecording() {
    const audioTracks = localStream.getAudioTracks();

    if (audioTracks.length > 0) {
        const audioStream = new MediaStream(audioTracks);
        mediaRecorder = new MediaRecorder(audioStream);

        mediaRecorder.ondataavailable = (event) => {
            if (event.data.size > 0) {
                audioChunks.push(event.data);
            }
        };

        mediaRecorder.onstop = async () => {
            if (audioChunks.length > 0) {
                const currentChunks = [...audioChunks];
                audioChunks = [];
                try {
                    await saveAudioToWav(currentChunks);
                    if (!isCaptureStopping && mediaRecorder.state === 'inactive') {
                        mediaRecorder.start(1000);
                    }
                } catch (error) {
                    console.error('오디오 저장 중 오류 발생:', error);
                }
            }
        };

        mediaRecorder.start(1000);

        recordingInterval = setInterval(() => {
            if (audioChunks.length > 0 && mediaRecorder.state === 'recording') {
                mediaRecorder.stop();
            }
        }, 600000);
    } else {
        console.error('오디오 트랙을 찾을 수 없습니다.');
    }
}

// WAV 변환 함수
async function convertToWav(audioBuffer) {
    return new Blob([audioBufferToWav(audioBuffer)], { type: 'audio/wav' });
}

function audioBufferToWav(buffer) {
    const numOfChannels = buffer.numberOfChannels;
    const sampleRate = buffer.sampleRate;
    const length = buffer.length * numOfChannels * 2 + 44;
    const bufferArray = new Uint8Array(length);
    const view = new DataView(bufferArray.buffer);

    writeString(view, 0, 'RIFF');
    view.setUint32(4, length - 8, true);
    writeString(view, 8, 'WAVE');
    writeString(view, 12, 'fmt ');
    view.setUint32(16, 16, true);
    view.setUint16(20, 1, true);
    view.setUint16(22, numOfChannels, true);
    view.setUint32(24, sampleRate, true);
    view.setUint32(28, sampleRate * 2 * numOfChannels, true);
    view.setUint16(32, numOfChannels * 2, true);
    view.setUint16(34, 16, true);
    writeString(view, 36, 'data');
    view.setUint32(40, length - 44, true);

    let offset = 44;
    for (let i = 0; i < buffer.length; i++) {
        for (let channel = 0; channel < numOfChannels; channel++) {
            const sample = Math.max(-1, Math.min(1, buffer.getChannelData(channel)[i]));
            view.setInt16(offset, sample < 0 ? sample * 0x8000 : sample * 0x7FFF, true);
            offset += 2;
        }
    }

    return bufferArray;
}

function writeString(view, offset, string) {
    for (let i = 0; i < string.length; i++) {
        view.setUint8(offset + i, string.charCodeAt(i));
    }
}

document.addEventListener('DOMContentLoaded', function() {
    const tabs = document.querySelectorAll(".tab-button");
    const summaries = document.querySelectorAll(".summaryContent");

    tabs.forEach(tab => {
        tab.addEventListener("click", function() {
            // 모든 탭 비활성화
            tabs.forEach(t => t.classList.remove("active"));

            // 모든 요약 내용과 로딩 컨테이너 숨기기
            summaries.forEach(summary => {
                summary.style.display = "none";
            });
            document.querySelectorAll('.loading-container').forEach(container => {
                container.style.display = "none";
            });

            // 현재 탭 활성화
            this.classList.add("active");

            // 현재 탭에 해당하는 컨텐츠와 로딩 컨테이너 찾기
            const currentContent = document.getElementById(`${this.id}Summ`);
            const loadingContainerId = `loadingContainer${Array.from(tabs).indexOf(this) + 1}`;
            const currentLoadingContainer = document.getElementById(loadingContainerId);

            if (currentContent.innerHTML.includes("불러오는 중입니다")) {
                currentLoadingContainer.style.display = "flex";
                currentContent.style.display = "none";
            } else {
                currentLoadingContainer.style.display = "none";
                currentContent.style.display = "block";
            }
        });
    });
});

// 오디오 WAV 파일 저장 코드 FastAPI로 전송
async function saveAudioToWav(chunks) {
    if (!chunks || chunks.length === 0) {
        console.log('저장할 오디오 데이터가 없습니다.');
        return;
    }

    try {
        const blob = new Blob(chunks, { type: 'audio/webm' });
        if (blob.size === 0) {
            console.log('빈 오디오 데이터입니다.');
            return;
        }

        const arrayBuffer = await blob.arrayBuffer();
        const audioContext = new (window.AudioContext || window.webkitAudioContext)();
        const audioBuffer = await audioContext.decodeAudioData(arrayBuffer);
        const wavBlob = await convertToWav(audioBuffer);

        // WAV 파일을 서버로 전송
        const sanitizedFilename = sanitizeFilename(`audio-${new Date().toISOString()}.wav`);
        const meetingName = document.querySelector("#meetingTitle p").textContent.split(" : ")[1];
        const formData = new FormData();
        formData.append('audio', wavBlob, sanitizedFilename);
        formData.append('meeting_name', meetingName);
        formData.append('status', "ing");

        // 로딩 GIF 표시 및 요약 내용 숨기기
        const loadingGifs = document.querySelectorAll('[id^="loadingGif"]');
        const summaryContents = document.querySelectorAll('.summaryContent');

        summaryContents.forEach(content => content.style.display = 'none');
        loadingGifs.forEach(gif => gif.style.display = 'block');


        try {
            const response = await fetch('https://mate-fastapi-hxaybhbzakhvfvhf.koreacentral-01.azurewebsites.net/summarize_meeting', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                const summary = await response.json();

                // 로딩 GIF 숨기기
                loadingGifs.forEach(gif => gif.style.display = 'none');

                // 현재 활성화된 탭 찾기
                const activeTab = document.querySelector('.tab-button.active');
                const activeContentId = `${activeTab.id}Summ`;

                // 각 요약 내용 업데이트
                updateSummaryContent('total', summary.total);
                updateSummaryContent('topicwise', summary.topicwise);
                updateSummaryContent('posneg', summary.posneg);
                updateSummaryContent('TODOList', summary.todo);

                // 현재 활성화된 탭의 내용만 표시
                document.getElementById(activeContentId).style.display = 'block';

                console.log('오디오 업로드 성공', summary);
            } else {
                handleSummaryError('데이터를 불러오는 중 오류가 발생했습니다.');
            }
        } catch (error) {
            handleSummaryError('데이터를 불러오는 중 오류가 발생했습니다.');
            console.error('오디오 업로드 중 오류 발생:', error);
        }
    } catch (error) {
        console.error('오디오 저장 중 오류 발생:', error);
        throw error;
    }
}

// 요약 내용 업데이트 헬퍼 함수
function updateSummaryContent(type, content) {
    const summaryElement = document.getElementById(`${type}Summ`);
    const loadingContainer = document.getElementById(`loadingContainer${getTabIndex(type)}`);
    const isActiveTab = document.getElementById(type).classList.contains('active');

    if (content && content.trim() !== "") {
        summaryElement.innerHTML = content
            .replace(/●/g, "&nbsp;●")
            .replace(/\n -/g, "\n &nbsp;&nbsp;-")
            .replace(/\n-/g, "\n &nbsp;&nbsp;-");

        loadingContainer.style.display = "none";
        summaryElement.style.display = isActiveTab ? "block" : "none";

    } else {
        summaryElement.textContent = "요약이 존재하지 않습니다.";
        loadingContainer.style.display = "none";
        summaryElement.style.display = isActiveTab ? "block" : "none";
    }
}

// 탭 인덱스 가져오는 헬퍼 함수
function getTabIndex(type) {
    const tabTypes = ['total', 'topicwise', 'posneg', 'TODOList'];
    return tabTypes.indexOf(type) + 1;
}

// 에러 처리 헬퍼 함수
function handleSummaryError(errorMessage) {
    const loadingGifs = document.querySelectorAll('[id^="loadingGif"]');
    const summaryContents = document.querySelectorAll('.summaryContent');

    loadingGifs.forEach(gif => gif.style.display = 'none');
    summaryContents.forEach(content => {
        content.style.display = 'block';
        content.textContent = errorMessage;
    });
}

// 캡처 중지
function stopCapture() {
    isCaptureStopping = true;

    stopScreenCapture();
    stopAudioRecording();
}

function stopScreenCapture() {
    if (localStream) {
        localStream.getTracks().forEach(track => track.stop());
        document.getElementById('localVideo').srcObject = null;
    }
    clearInterval(captureInterval);
}

function stopAudioRecording() {
    clearInterval(recordingInterval);

    if (mediaRecorder && mediaRecorder.state === 'recording') {
        mediaRecorder.stop();
    }
}

// 이벤트 리스너 설정
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('startButton').addEventListener('click', startCapture);
    document.getElementById('stopButton').addEventListener('click', stopCapture);
});