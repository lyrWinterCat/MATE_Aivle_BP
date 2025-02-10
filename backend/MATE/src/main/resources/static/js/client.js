let meetingId;
let previousParticipants = []; // 이전 참여자 리스트 저장
let previousStartTime = "";
let previousBreakTime = "";
let isupdatedStartTime=false;
//Interval
let participantFetchInterval;
let summaryFetchInterval;
let breakTimeFetchInterval;
let shareFetchInterval;
//회의 요약 가져오기
let topic = "";
let yesno = "";
let todo = "";
let total = "";
let screen = "";
let isEmpty = 0;
let hasActivatedFirstTab = false;
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
// 회의 참여자 갱신 (SSE 없이 setInterval 사용)
function startMeetingUpdates() {
    console.log("회의 참여자 갱신 시작");
    participantFetchInterval = setInterval(() => fetchParticipants(meetingId), 5000);
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
            const newParticipants = data.meetingParticipants.map(p => p.userName);

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
            if (previousStartTime!="" && previousStartTime !== newStartTime) {
                console.warn("회의 시작 시간이 추가 및 됨. 자동 업데이트 중지.");
                stopMeetingUpdates(); // 자동 갱신 중지
                document.getElementById("screenshareButton").classList.remove("noactive");
                console.log(">>> [data.meetingStartTime] :", newStartTime);
            } else {
                previousStartTime = newStartTime; // 기존 값 업데이트
            }
        },
        error: function (xhr, textStatus, errorThrown) {
            console.error("AJAX 요청 실패:", textStatus, errorThrown);

            if (textStatus === "error" && errorThrown === "") {
                console.warn("서버에 연결할 수 없습니다. (ERR_CONNECTION_REFUSED)");
                handleConnectionError();
            } else if (textStatus === "timeout") {
                console.warn("요청 시간이 초과되었습니다. (서버 응답 없음)");
                handleConnectionError();
            } else {
                console.warn("기타 네트워크 오류:", textStatus);
            }
        }
    });
}
function handleConnectionError() {
    console.log("🚨 서버에 연결할 수 없습니다. 네트워크를 확인하세요.");
}
// 자동 업데이트 중지
function stopMeetingUpdates() {
    console.warn("회의참여자 자동 업데이트 중지");
    if (participantFetchInterval) {
        clearInterval(participantFetchInterval);
        participantFetchInterval = null;
        //휴식시간 업데이트
        breakTimeFetchInterval = setInterval(() => fetchBreakTime(), 5000);
    }
}
//회의 요약가져오기
function fetchSummary(){
    $.ajax({
        url: `/meeting/client/${meetingId}/summary`, // API 엔드포인트
        method: 'POST',
        dataType:'json',
        success: function (data) {
            // data로 상태확인
            let element = document.getElementById("meetingImage");
            if (element) {
                element.style.display = "none";
            } else {
                console.log("meetingImage 요소가 존재하지 않습니다.");
            }
            if(data.body==="요약 데이터가 없습니다."){
                //document.getElementById("mode-wait").textContent = "요약 데이터가 없습니다.";
                //DB insert과정에서 걸릴것 같아 console.log로만
                console.warn(data.body);
                isEmpty = 0;
                return;
            }else{
                console.log(data);
                 // 응답을 콘솔에 출력
                yesno = data.body.summaryPositiveNegative;
                topic = data.body.summaryTopic;
                todo = data.body.todoList;
                total = data.body.summaryTotal;
                isEmpty = 1;
                // 첫번째 탭 보여주기
                if (!hasActivatedFirstTab) {
                    setTimeout(() => {
                        activateFirstTab();
                        hasActivatedFirstTab = true;
                    }, 100);
                }
            }
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
// 회의 요약 업데이트 성공 시 첫번째 탭 자동 선택
function activateFirstTab() {
    const firstTab = document.getElementById("summary-categori");
    const topicText = document.getElementById("categori-content-area");
    const notext = document.getElementById("notext");
    const caution = document.getElementById("caution");

    if (firstTab && topic.trim() !== "") {
        // 모든 탭에서 active 클래스 제거
        document.querySelectorAll(".tab").forEach(tab => tab.classList.remove("active"));
        // 첫 번째 탭 활성화
        firstTab.classList.add("active");
        notext.style.display = "none"; // 기본 안내문 숨기기

        // 요약 내용 표시
        topicText.style.display = "block";
        caution.style.display = "block";
        topicText.innerHTML = topic.replace(/\n/g, "<br>");
        console.log(" 첫 번째 탭 자동 활성화 완료");
        clearInterval(summaryFetchInterval);
    }else{
        console.warn("요약 데이터가 없습니다.");
    }
}
//회의 쉬는시간 가져오기
function fetchBreakTime() {
    $.ajax({
        url: `/meeting/client/${meetingId}/breakTime`, // API 엔드포인트
        method: 'POST',
        dataType:'json',
        success: function (data) {
            console.log(data); // 응답을 콘솔에 출력
            if (!data || data.meetingBreakTime === null || data.meetingBreakTime === undefined) {
                console.warn("회의 쉬는 시간이 없습니다.");
                return;
            }
            console.log("회의 휴식 시간:", data.meetingBreakTime);
            const newBreakTime = data.meetingBreakTime;
            if (previousBreakTime !== newBreakTime) {
                console.warn("회의 휴식 시간이 변경됨.");
                //요약 업데이트
                summaryFetchInterval = setInterval(() => fetchSummary(), 5000);
            } else {
                previousBreakTime = newBreakTime; // 기존 값 업데이트
            }
        },
        error: function (error) {
            console.error('쉬는 시간 가져오기 오류:', error);
        }
    });
}
//자료 요약
function fetchScreenSummary(){
     $.ajax({
         url: `/meeting/client/${meetingId}/imagesummary`, // API 엔드포인트
         method: 'POST',
         dataType: 'text',
         success: function (data) {

             if (data.trim() === "요약 데이터가 없습니다.") {
                 console.warn("요약 데이터가 없습니다.");
                 document.getElementById("document-summary").innerHTML = "<p>요약 데이터가 없습니다.</p>";
             } else {
                 console.log("공유자료 요약 완료");
                 let element = document.getElementById("image-notext");
                 if (element) {
                     element.style.display = "none";
                 } else {
                     console.warn("image-notext 요소가 존재하지 않습니다.");
                 }
                 document.getElementById("document-summary").innerHTML = data.replace(/\n/g, "<br>");
             }
         },
         error: function (xhr) {
             if (xhr.status === 404) {
                 console.warn("공유자료 요약 데이터 없음 (404)");
             } else {
                 console.error("공유자료 요약 가져오기 오류:", xhr);
             }
        }
    });
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
            clearInterval(breakTimeFetchInterval);
            clearInterval(summaryFetchInterval);
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
document.getElementById('screenshareButton').addEventListener('click', function () {
    //이미지 요약 업데이트
   fetchScreenSummary();
});
function moveMain() {
    window.location.href = "/"; //로고
}
