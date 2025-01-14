let stompClient = null;
let localStream = null;
let mediaRecorder;
let audioChunks = [];
let recordingInterval;
let captureInterval;
let ws = null;
let isCaptureStopping = false;

/* WebSocket 연결 관련 코드 (FastAPI 구현 후 주석 해제)
// WebSocket 연결 설정
function connectWebSocket() {
    // FastAPI WebSocket 엔드포인트로 연결
    ws = new WebSocket('ws://your-fastapi-server/ws/media');

    ws.onopen = () => {
        console.log('WebSocket 연결 성공');
    };

    ws.onmessage = (event) => {
        // FastAPI로부터 받은 메시지 처리
        const response = JSON.parse(event.data);
        handleServerResponse(response);
    };

    ws.onclose = () => {
        console.log('WebSocket 연결 종료');
        // 재연결 로직 구현
        setTimeout(connectWebSocket, 3000);
    };

    ws.onerror = (error) => {
        console.error('WebSocket 에러:', error);
    };

    return ws;
}

// 서버 응답 처리
function handleServerResponse(response) {
    switch(response.type) {
        case 'transcription':
            console.log('음성 인식 결과:', response.text);
            break;
        case 'screen_capture':
            console.log('화면 캡처 저장 결과:', response.status);
            break;
        default:
            console.log('알 수 없는 응답:', response);
    }
}

// 화면 캡처 데이터 전송
function sendScreenCapture(imageBlob) {
    if (ws && ws.readyState === WebSocket.OPEN) {
        const reader = new FileReader();
        reader.readAsDataURL(imageBlob);
        reader.onloadend = () => {
            const base64Image = reader.result.split(',')[1];
            ws.send(JSON.stringify({
                type: 'screen_capture',
                data: base64Image,
                timestamp: new Date().toISOString()
            }));
        };
    }
}

// 오디오 데이터 전송
function sendAudioData(audioData) {
    if (ws && ws.readyState === WebSocket.OPEN) {
        const reader = new FileReader();
        reader.readAsDataURL(audioData);
        reader.onloadend = () => {
            const base64Audio = reader.result.split(',')[1];
            ws.send(JSON.stringify({
                type: 'audio',
                data: base64Audio,
                timestamp: new Date().toISOString()
            }));
        };
    }
}
*/


// 화면 및 오디오 스트림 시작 함수
async function startCapture() {
    try {
        localStream = await navigator.mediaDevices.getDisplayMedia({
            video: true,
            audio: true
        });
        document.getElementById('localVideo').srcObject = localStream;
        startAudioRecording();
        startScreenCapture();
    } catch (error) {
        console.error('미디어 캡처 중 오류 발생:', error);
    }
}

// 화면 캡쳐 시작
function startScreenCapture() {
    const video = document.getElementById('localVideo');
    const canvas = document.createElement('canvas');
    const context = canvas.getContext('2d');

    captureInterval = setInterval(() => {
        if (video.videoWidth === 0 || video.videoHeight === 0) return;

        canvas.width = video.videoWidth;
        canvas.height = video.videoHeight;
        context.drawImage(video, 0, 0, canvas.width, canvas.height);

        canvas.toBlob((blob) => {
            const url = URL.createObjectURL(blob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = `screenshot-${new Date().toISOString()}.png`;
            document.body.appendChild(a);
            a.click();
            setTimeout(() => {
                document.body.removeChild(a);
                window.URL.revokeObjectURL(url);
            }, 100);
        }, 'image/png');
    }, 30000); // 30초에 1장씩 캡쳐. 이 값을 변경해서 캡쳐 시간을 조절할 수 있음
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
                // sendAudioData(event.data); // FastAPI로 실시간 전송 (구현 후 주석 해제)
            }
        };

        mediaRecorder.onstop = async () => {
            if (audioChunks.length > 0) {
                const currentChunks = [...audioChunks];
                audioChunks = [];
                try {
                    await saveAudioToWav(currentChunks);
                    // 녹음이 계속되어야 하고, 중지 버튼이 눌리지 않았을 때만 다시 시작
                    if (!isCaptureStopping && mediaRecorder.state === 'inactive') {
                        mediaRecorder.start(1000);
                    }
                } catch (error) {
                    console.error('오디오 저장 중 오류 발생:', error);
                }
            }
        };

        mediaRecorder.start(1000);

// 1분마다 오디오 저장
        recordingInterval = setInterval(() => {
            if (audioChunks.length > 0 && mediaRecorder.state === 'recording') {
                mediaRecorder.stop();
            }
        }, 60000);
    } else {
        console.error('오디오 트랙을 찾을 수 없습니다.');
    }
}

// 양파님이 요구하신 WAV 변환 및 저장 함수들
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

// 오디오 WAV 파일 저장 함수
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

        try {
            const audioBuffer = await audioContext.decodeAudioData(arrayBuffer);
            const wavBlob = await convertToWav(audioBuffer);

            const url = URL.createObjectURL(wavBlob);
            const a = document.createElement('a');
            a.style.display = 'none';
            a.href = url;
            a.download = `audio-${new Date().toISOString()}.wav`;
            document.body.appendChild(a);
            a.click();
            setTimeout(() => {
                document.body.removeChild(a);
                window.URL.revokeObjectURL(url);
            }, 100);

            console.log('오디오 파일이 저장되었습니다.');
        } catch (decodeError) {
            console.error('오디오 디코딩 중 오류 발생:', decodeError);
            throw decodeError;
        }
    } catch (error) {
        console.error('오디오 저장 중 오류 발생:', error);
        throw error;
    }
}

// 캡처 중지 (중지하기 버튼 클릭 이벤트)
function stopCapture() {
    isCaptureStopping = true;  // 중지 상태 플래그 설정

    stopScreenCapture(); // 화면 캡처 중지
    stopAudioRecording(); // 오디오 녹음 중지
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

        // WebSocket 연결 종료 (구현 후 주석 해제)
        // if (ws) {
        //     ws.close();
        // }
}

// 이벤트 리스너 설정
document.addEventListener('DOMContentLoaded', function() {
    document.getElementById('startButton').addEventListener('click', startCapture);
    document.getElementById('stopButton').addEventListener('click', stopCapture);
});
