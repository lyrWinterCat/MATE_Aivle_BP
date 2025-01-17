let stompClient = null;
let peerConnection = null;
const remoteVideo = document.getElementById('remoteVideo');
const startButton = document.getElementById('startButton');

const configuration = {
    iceServers: [
        { urls: 'stun:stun.l.google.com:19302' }
    ]
};

function connect() {
    const socket = new SockJS('/ws-endpoint');
    stompClient = Stomp.over(socket);

    // 디버깅을 위해 로그 추가
    stompClient.debug = function(str) {
        console.log('STOMP: ', str);
    };

    stompClient.connect({},
        frame => {
            console.log('Connected:', frame);
            onConnected();
        },
        error => {
            console.error('STOMP error:', error);
            onError(error);
        }
    );
}

function onConnected() {
    console.log('WebSocket 연결 성공');
    stompClient.subscribe('/topic/public', onMessageReceived);
    initializePeerConnection();
    // 시청 준비 완료 알림
    stompClient.send("/app/signal",
        {},
        JSON.stringify({type: 'viewer-ready'})
    );
}

function onError(error) {
    console.error('WebSocket 연결 실패:', error);
}

function initializePeerConnection() {
    peerConnection = new RTCPeerConnection(configuration);

    peerConnection.ontrack = (event) => {
        if (remoteVideo.srcObject !== event.streams[0]) {
            remoteVideo.srcObject = event.streams[0];
        }
    };

    peerConnection.onicecandidate = (event) => {
        if (event.candidate) {
            stompClient.send("/app/ice-candidate",
                {},
                JSON.stringify(event.candidate)
            );
        }
    };

    // 연결 상태 모니터링
    peerConnection.onconnectionstatechange = () => {
        console.log('Connection state:', peerConnection.connectionState);
    };

    peerConnection.oniceconnectionstatechange = () => {
        console.log('ICE connection state:', peerConnection.iceConnectionState);
    };
}

async function onMessageReceived(payload) {
    const message = JSON.parse(payload.body);

    switch(message.type) {
        case 'offer':
            try {
                await peerConnection.setRemoteDescription(new RTCSessionDescription(message.payload));
                const answer = await peerConnection.createAnswer();
                await peerConnection.setLocalDescription(answer);

                stompClient.send("/app/signal",
                    {},
                    JSON.stringify({
                        type: 'answer',
                        payload: answer
                    })
                );
            } catch (error) {
                console.error('Error handling offer:', error);
            }
            break;

        case 'ice-candidate':
            try {
                await peerConnection.addIceCandidate(new RTCIceCandidate(message));
            } catch (error) {
                console.error('Error adding ice candidate:', error);
            }
            break;
    }
}

startButton.addEventListener('click', connect);