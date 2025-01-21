let stompClient = null;
let peerConnection = null;
const remoteVideo = document.getElementById('remoteVideo');

const configuration = {
    iceServers: [
        { urls: 'stun:stun.l.google.com:19302' }
    ]
};

function connect() {
    console.log('연결 시도 중...');
    const socket = new SockJS('/ws-endpoint');
    stompClient = Stomp.over(socket);

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
    stompClient.subscribe('/topic/public', onMessageReceived, {
        'id': 'viewer-subscription'
    });
    console.log('토픽 구독 완료');

    initializePeerConnection();

    // 시청 준비 완료 알림 전송
    const readyMessage = {
        type: 'viewer-ready'
    };
    console.log('시청 준비 메시지 전송:', readyMessage);
    stompClient.send("/app/signal", {}, JSON.stringify(readyMessage));
}

function onError(error) {
    console.error('WebSocket 연결 실패:', error);
    setTimeout(() => {
        console.log('재연결 시도...');
        connect();
    }, 3000);
}

function initializePeerConnection() {
    console.log('PeerConnection 초기화 시작');
    if (peerConnection) {
        peerConnection.close();
    }
    peerConnection = new RTCPeerConnection(configuration);

    peerConnection.ontrack = (event) => {
        console.log('트랙 수신됨:', event);
        if (event.streams && event.streams[0]) {
            console.log('비디오 스트림 설정');
            remoteVideo.srcObject = event.streams[0];
            remoteVideo.play().catch(e => console.error('비디오 재생 실패:', e));
        }
    };

    peerConnection.onicecandidate = (event) => {
        if (event.candidate) {
            console.log('ICE candidate 전송:', event.candidate);
            stompClient.send("/app/ice-candidate", {}, JSON.stringify(event.candidate));
        }
    };

    peerConnection.onconnectionstatechange = () => {
        console.log('Connection state 변경:', peerConnection.connectionState);
        if (peerConnection.connectionState === 'failed') {
            console.log('연결 실패, 재시도...');
            initializePeerConnection();
        }
    };

    peerConnection.oniceconnectionstatechange = () => {
        console.log('ICE connection state 변경:', peerConnection.iceConnectionState);
    };

    console.log('PeerConnection 초기화 완료');
}

async function onMessageReceived(payload) {
    console.log('메시지 수신:', payload.body);
    const message = JSON.parse(payload.body);

    switch(message.type) {
        case 'offer':
            console.log('Offer 수신');
            try {
                await peerConnection.setRemoteDescription(new RTCSessionDescription(message.payload));
                console.log('Remote description 설정 완료');

                const answer = await peerConnection.createAnswer();
                console.log('Answer 생성');

                await peerConnection.setLocalDescription(answer);
                console.log('Local description 설정 완료');

                stompClient.send("/app/signal", {}, JSON.stringify({
                    type: 'answer',
                    payload: answer
                }));
                console.log('Answer 전송 완료');
            } catch (error) {
                console.error('Offer 처리 중 에러:', error);
            }
            break;

        case 'ice-candidate':
            console.log('ICE candidate 수신');
            try {
                await peerConnection.addIceCandidate(new RTCIceCandidate(message));
                console.log('ICE candidate 추가 완료');
            } catch (error) {
                console.error('ICE candidate 추가 중 에러:', error);
            }
            break;

        default:
            console.log('알 수 없는 메시지 타입:', message.type);
    }
}

document.addEventListener('DOMContentLoaded', () => {
    console.log('페이지 로드됨, 연결 시작');
    connect();

    // 비디오 엘리먼트 준비 상태 확인
    remoteVideo.addEventListener('loadedmetadata', () => {
        console.log('비디오 메타데이터 로드됨');
    });

    remoteVideo.addEventListener('play', () => {
        console.log('비디오 재생 시작');
    });

    remoteVideo.addEventListener('error', (e) => {
        console.error('비디오 에러:', e);
    });
});