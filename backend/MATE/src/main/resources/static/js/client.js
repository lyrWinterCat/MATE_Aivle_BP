let meetingId;
let previousParticipants = []; // 이전 참여자 리스트 저장
let participantFetchInterval;
function fetchParticipants() {
    $.ajax({
        url: `/meeting/${meetingId}/participants`, // API 엔드포인트
        method: 'GET',
        success: function(data) {
            console.log(data); // 응답을 콘솔에 출력
            const newParticipants = data.meetingParticipants.map(p => p.userName).sort();
            if (JSON.stringify(previousParticipants) !== JSON.stringify(newParticipants)) {
                console.log("참여자 변경 감지: UI 업데이트 중...");
                updateParticipantList(newParticipants); // UI 업데이트
                previousParticipants = newParticipants; // 변경 사항 저장
            } else {
                console.log("참여자 목록 변경 없음");
            }
            // 참여자 수 업데이트
            $('#participantCount').text(data.participantCount);
        },
        error: function(error) {
            console.error('참여자 정보 가져오기 오류:', error);
        }
    });
}

// UI 업데이트 함수
function updateParticipantList(newParticipants) {
    console.log("UI 업데이트");
    const participantList = $('#participants');
    //participantList.empty(); // 기존 목록 초기화

    if (newParticipants.length > 0) {
        newParticipants.forEach(name => {
            participantList.append(`<p>${name}</p>`); // 참여자 리스트 추가
        });
        console.log("UI 업데이트 > 완료");
    } else {
        participantList.append('<p>참여자가 없습니다.</p>'); // 참여자가 없을 경우 메시지 표시
    }
}

document.addEventListener("DOMContentLoaded", function () {
    meetingId = document.getElementById("meetingId").value;
    console.log(">>> meetingId : "+meetingId);

    participantFetchInterval = setInterval(function(){
        fetchParticipants(); //실시간 회의 참여자 업데이트
    }, 5000);

    //탭버튼 이벤트
    const tabs = document.querySelectorAll(".tab");
    tabs.forEach(tab => {
        tab.addEventListener("click", function () {
            // 모든 탭에서 active 클래스 제거
            tabs.forEach(t => t.classList.remove("active"));

            // 클릭한 탭에 active 클래스 추가
            this.classList.add("active");

            if (this.id === "summary") {

            } else if (this.id === "summary-categori") {

            } else if (this.id === "summary-yesno") {

            } else if (this.id === "summary-total") {

            }
        });
    });
});

const eventSource = new EventSource(`/meeting/${meetingId}/participants`);

eventSource.onmessage = function(event) {
    const updatedParticipants = JSON.parse(event.data);
    console.log("실시간 업데이트 감지:", updatedParticipants);
    updateParticipantList(updatedParticipants);
};

eventSource.onerror = function() {
    console.error("SSE 연결 종료됨");
};

//회의 종료하기 - 회의 종료 시간을 기록하는 함수
function endMeeting() {
    $.ajax({
        url: `/meeting/${meetingId}/end`,
        method: 'POST',
        success: function(response) {
            console.log('회의 종료 시간 기록:', response);
            fetchDomain().then(domain => {
                console.log('도메인:', domain); // 도메인 로그 추가
                window.location.href = "/user/userMain";
            });
        },
        error: function(error) {
            console.error('회의 종료 시간 기록 오류:', error);
        }
    });
}
document.getElementById('endMeetingButton').addEventListener('click', function() {
    endMeeting(); // 회의 종료
});