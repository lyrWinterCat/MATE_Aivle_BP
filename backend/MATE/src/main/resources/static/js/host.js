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
                const response = await fetch('http://127.0.0.1:8000/post_image', {
                    method: 'POST',
                    body: formData
                });

                if (response.ok) {
                    console.log('이미지 업로드 성공');
                } else {
                    console.error('이미지 업로드 실패');
                }
            } catch (error) {
                console.error('이미지 전송 중 오류:', error);
            }
        }, 'image/png');
    }, 30000);
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
        }, 60000);
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

        try {
            const audioBuffer = await audioContext.decodeAudioData(arrayBuffer);
            const wavBlob = await convertToWav(audioBuffer);

            // WAV 파일을 서버로 전송
            const sanitizedFilename = sanitizeFilename(`audio-${new Date().toISOString()}.wav`);
            const formData = new FormData();
            formData.append('audio', wavBlob, sanitizedFilename);

            const response = await fetch('http://127.0.0.1:8000/post_audio', {
                method: 'POST',
                body: formData
            });

            if (response.ok) {
                console.log('오디오 업로드 성공');
            } else {
                console.error('오디오 업로드 실패');
            }

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

