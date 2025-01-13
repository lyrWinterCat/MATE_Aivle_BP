let stompClient = null;
let localStream = null;

function connect() {
    const socket = new SockJS('/screen-share');
    stompClient = Stomp.over(socket);
    stompClient.connect({}, function(frame) {
        console.log('Connected: ' + frame);
        subscribeToScreenData();
    });
}

function subscribeToScreenData() {
    stompClient.subscribe('/topic/screen-data', function(response) {
        const screenData = JSON.parse(response.body);
        handleReceivedScreenData(screenData);
    });
}

async function startScreenShare() {
    try {
        localStream = await navigator.mediaDevices.getDisplayMedia({
            video: true
        });
        document.getElementById('localVideo').srcObject = localStream;

        // 화면 캡처 및 전송 로직
        const track = localStream.getVideoTracks()[0];
        const imageCapture = new ImageCapture(track);

        // 주기적으로 화면 캡처 및 전송
        setInterval(async () => {
            const bitmap = await imageCapture.grabFrame();
            const canvas = document.createElement('canvas');
            canvas.width = bitmap.width;
            canvas.height = bitmap.height;
            const context = canvas.getContext('2d');
            context.drawImage(bitmap, 0, 0);

            const data = canvas.toDataURL('image/jpeg', 0.5);
            sendScreenData(data);
        }, 1000/30); // 30fps
    } catch (error) {
        console.error('Error accessing screen:', error);
    }
}

function sendScreenData(data) {
    if (stompClient) {
        const screenData = {
            data: data,
            sender: 'user-' + Math.random().toString(36).substr(2, 9),
            timestamp: Date.now()
        };
        stompClient.send("/app/screen-data", {}, JSON.stringify(screenData));
    }
}

function handleReceivedScreenData(screenData) {
    const remoteVideo = document.getElementById('remoteVideo');
    remoteVideo.src = screenData.data;
}

// 이벤트 리스너 설정
document.addEventListener('DOMContentLoaded', function() {
    connect();

    document.getElementById('startButton').addEventListener('click', startScreenShare);
    document.getElementById('stopButton').addEventListener('click', function() {
        if (localStream) {
            localStream.getTracks().forEach(track => track.stop());
            document.getElementById('localVideo').srcObject = null;
        }
    });
});