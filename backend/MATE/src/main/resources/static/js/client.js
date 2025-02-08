let meetingId;
let previousParticipants = []; // 이전 참여자 리스트 저장
let participantFetchInterval;
let previousStartTime = "";

// meetingId 셋팅 대기
function waitForMeetingId() {
    const checkInterval = setInterval(() => {
        meetingId = document.getElementById("meetingId").value;

        if (meetingId && meetingId !== "undefined") {
            console.log("meetingId 설정됨 : " + meetingId);
            clearInterval(checkInterval);
            startMeetingUpdates(); // fetchParticipants 실행
        }
    }, 500);
}

// 참여자 목록 새로고침
function fetchParticipants(meetingId) {
    if (!meetingId || meetingId === "undefined") {
        console.warn("meetingId가 undefined로 떨어짐");
        return;
    }
    $.ajax({
        url: `/meeting/${meetingId}/client/participants`, // API 엔드포인트
        method: 'GET',
        success: function (data) {
            console.log(data); // 응답을 콘솔에 출력
            const newParticipants = data.meetingParticipants.map(p => p.userName).sort();
            if (JSON.stringify(previousParticipants) !== JSON.stringify(newParticipants)) {
                console.log("참여자 변경 감지: UI 업데이트 중...");
                updateParticipantList(newParticipants); // UI 업데이트
                previousParticipants = [...newParticipants]; // 변경 사항 저장
            } else {
                console.log("참여자 목록 변경 없음");
            }
            // 참여자 수 업데이트
            $('#participantCount').text(data.participantCount);
            const newStartTime = data.meetingStartTime;
            if (previousStartTime && previousStartTime !== newStartTime) {
                console.warn("회의 시작 시간이 변경됨. 자동 업데이트 중지.");
                stopMeetingUpdates(); // 자동 갱신 중지
            } else {
                previousStartTime = newStartTime; // 기존 값 업데이트
            }
            console.log(">>> [data.meetingStartTime] :", newStartTime);
        },
        error: function (error) {
            console.error('참여자 정보 가져오기 오류:', error);
        }
    });
}

// 자동 업데이트 중지
function stopMeetingUpdates() {
    console.log("자동 업데이트 중지");

    if (participantFetchInterval) {
        clearInterval(participantFetchInterval);
        participantFetchInterval = null;
    }
}

// UI 업데이트 함수
function updateParticipantList(newParticipants) {
    console.log("UI 업데이트");
    const participantList = $('#participants');
    participantList.empty(); // 기존 목록 초기화

    if (newParticipants.length > 0) {
        newParticipants.forEach(name => {
            participantList.append(`<p>${name}</p>`); // 참여자 리스트 추가
        });
        console.log("UI 업데이트 > 완료");
    } else {
        participantList.append('<p>참여자가 없습니다.</p>'); // 참여자가 없을 경우 메시지 표시
    }
}

// 회의 참여자 갱신 (SSE 없이 setInterval 사용)
function startMeetingUpdates() {
    console.log("회의 참여자 갱신 시작");
    participantFetchInterval = setInterval(() => fetchParticipants(meetingId), 5000);
}

document.addEventListener("DOMContentLoaded", function () {
    // 미팅ID 확인 시 참여자 목록 갱신 기능
    waitForMeetingId();

    // 탭 버튼 이벤트
    const tabs = document.querySelectorAll(".tab");
    tabs.forEach(tab => {
        tab.addEventListener("click", function () {
            // 모든 탭에서 active 클래스 제거
            tabs.forEach(t => t.classList.remove("active"));

            // 모든 탭 + notext
            const notext = document.getElementById("notext");
            const categori = document.getElementById("categori-content-area");
            const yesno = document.getElementById("yesno-content-area");
            const todo = document.getElementById("todo-content-area");
            const total = document.getElementById("total-content-area");

            // 클릭한 탭에 active 클래스 추가
            this.classList.add("active");

            if (this.id === "summary-categori") {
                categori.style.display = "block";
                todo.style.display = "none";
                yesno.style.display = "none";
                total.style.display = "none";
                notext.style.display = "none";
            } else if (this.id === "summary-yesno") {
                yesno.style.display = "block";
                categori.style.display = "none";
                todo.style.display = "none";
                total.style.display = "none";
                notext.style.display = "none";
            } else if (this.id == "summary-todo") {
                todo.style.display = "block";
                categori.style.display = "none";
                total.style.display = "none";
                yesno.style.display = "none";
                notext.style.display = "none";
            } else if (this.id === "summary-total") {
                total.style.display = "block";
                categori.style.display = "none";
                todo.style.display = "none";
                yesno.style.display = "none";
                notext.style.display = "none";
            } else {
                notext.style.display = "block";
                todo.style.display = "none";
                categori.style.display = "none";
                yesno.style.display = "none";
                total.style.display = "none";
            }
        });
    });
});

// 회의 종료하기 - 회의 종료 시간을 기록하는 함수
function endMeeting() {
    $.ajax({
        url: `/meeting/${meetingId}/end`,
        method: 'POST',
        success: function (response) {
            console.log('회의 종료 시간 기록:', response);
            window.location.href = "/user/userMain";
        },
        error: function (error) {
            console.error('회의 종료 시간 기록 오류:', error);
        }
    });
}

document.getElementById('endMeetingButton').addEventListener('click', function () {
    endMeeting(); // 회의 종료
});
document.getElementById('endMeetingButton').addEventListener('click', function () {
    endMeeting(); // 회의 종료
});

function moveMain() {
    window.location.href = "/";
}
