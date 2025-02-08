let meetingId;
let previousParticipants = []; // 이전 참여자 리스트 저장
let participantFetchInterval;
let previousStartTime = "";
let summaryFetchInterval;

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
        url: `/meeting/client/${meetingId}/participants`, // API 엔드포인트
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
//회의 요약 가져오기
let topic = "";
let yesno = "";
let todo = "";
let total = "";
let isEmpty = 0;
function fetchSummary(){
    $.ajax({
        url: `/meeting/client/${meetingId}/summary`, // API 엔드포인트
        method: 'POST',
        dataType:'json',
        success: function (data) {
            // data로 상태확인
            if(data.body==="요약 데이터가 없습니다."){
                console.log(data.body);
                isEmpty = 0;
            }else{
                
                console.log(data);
                 // 응답을 콘솔에 출력
                yesno = data.body.summaryPositiveNegative;
                topic = data.body.summaryTopic;
                todo = data.body.todoList;
                total = data.body.summaryTotal;
                isEmpty = 1;
            }

            //clearInterval(summaryFetchInterval);
        },
        error: function (xhr) {
            console.warn("AJAX 요청 실패:", xhr);
            if (xhr.status === 404) {
                console.warn("요약 데이터 없음 (404)");
            } else {
                console.error("요약 가져오기 오류:", xhr);
            }
        }
    });
}
// 자동 업데이트 중지
function stopMeetingUpdates() {
    console.log("자동 업데이트 중지");

    if (participantFetchInterval) {
        clearInterval(participantFetchInterval);
        participantFetchInterval = null;
        //fetchSummary();
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
    summaryFetchInterval = setInterval(() => fetchSummary(), 5000);
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
            const topicText = document.getElementById("categori-content-area");
            const yesnoText = document.getElementById("yesno-content-area");
            const todoText = document.getElementById("todo-content-area");
            const totalText = document.getElementById("total-content-area");

            if (this.id === "summary-categori") {
                if(isEmpty===0){
                    this.classList.remove("active");
                    notext.style.display = "block";
                    topicText.style.display = "none";
                    yesnoText.style.display = "none";
                    todoText.style.display = "none";
                    totalText.style.display = "none";
                    return; // 함수 종료
                }else{
                    this.classList.add("active");
                    todoText.style.display = "none";
                    yesnoText.style.display = "none";
                    totalText.style.display = "none";
                    notext.style.display = "none";

                    topicText.style.display = "block";
                    topicText.textContent = topic;
                    topicText.innerHTML = topicText.textContent.replace(/\n/g, "<br>");
                }
            } else if (this.id === "summary-yesno") {
                if(isEmpty===0){
                    this.classList.remove("active");
                    notext.style.display = "block";
                    topicText.style.display = "none";
                    yesnoText.style.display = "none";
                    todoText.style.display = "none";
                    totalText.style.display = "none";
                    return; // 함수 종료
                }else{
                    this.classList.add("active");
                    topicText.style.display = "none";
                    todoText.style.display = "none";
                    totalText.style.display = "none";
                    notext.style.display = "none";

                    yesnoText.style.display = "block";
                    yesnoText.textContent = yesno;
                    yesnoText.innerHTML = yesnoText.textContent.replace(/\n/g, "<br>");
                }
            } else if (this.id == "summary-todo") {
                if(isEmpty===0){
                    this.classList.remove("active");
                    notext.style.display = "block";
                    topicText.style.display = "none";
                    yesnoText.style.display = "none";
                    todoText.style.display = "none";
                    totalText.style.display = "none";
                    return; // 함수 종료
                }else{
                    this.classList.add("active");
                    topicText.style.display = "none";
                    totalText.style.display = "none";
                    yesnoText.style.display = "none";
                    notext.style.display = "none";

                    todoText.style.display = "block";
                    todoText.textContent = todo;
                    todoText.innerHTML = todoText.textContent.replace(/\n/g, "<br>");
                }
            } else if (this.id === "summary-total") {
                if(isEmpty===0){
                    this.classList.remove("active");
                    notext.style.display = "block";
                    topicText.style.display = "none";
                    yesnoText.style.display = "none";
                    todoText.style.display = "none";
                    totalText.style.display = "none";
                    return; // 함수 종료
                }else{
                    this.classList.add("active");
                    topicText.style.display = "none";
                    todoText.style.display = "none";
                    yesnoText.style.display = "none";
                    notext.style.display = "none";
                    totalText.style.display = "block";
                    totalText.textContent = total;
                    totalText.innerHTML = totalText.textContent.replace(/\n/g, "<br>");
                }
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
