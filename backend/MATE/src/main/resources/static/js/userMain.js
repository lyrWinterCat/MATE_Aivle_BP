let meetingsData = [];
let isNewMeeting = true;

function moveMain() {
    window.location.href = "/user/userMain";
}

document.addEventListener("DOMContentLoaded", function() {
    const tabs = document.querySelectorAll(".tab-button");
    const meetingForm = document.getElementById("meetingForm");
    const meetingTitleInput = document.getElementById("meetingTitle-input");
    const meetingTitleSelect = document.getElementById("meetingTitle-select");
    const meetingUrlInputNew = document.getElementById("meetingUrl-input-new");
    const meetingUrlInputSelect = document.getElementById("meetingUrl-input-select");

    // 폼 제출 이벤트 처리
    meetingForm.addEventListener('submit', function(e) {
        e.preventDefault();
        
        if (isNewMeeting) {
            // 새 회의 생성 시 검증
            if (!meetingTitleInput.value.trim()) {
                alert("회의 제목을 입력하세요.");
                return;
            }
            if (!meetingUrlInputNew.value.trim()) {
                alert("회의 URL을 입력하세요.");
                return;
            }

            // 새 회의데이터 생성
            fetch("/meeting/create", {
                method: "POST",
                headers: { "Content-Type": "application/json" },
                body: JSON.stringify({
                    userId: userId,
                    meetingTitle: meetingTitleInput,
                    meetingUrl: meetingUrlInputNew
                })
            })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`서버 응답 오류: ${response.status} ${response.statusText}`);
                }
                return response.json(); // JSON 변환 (에러 발생 가능)
            })
            .then(data => {
                if (data.success) {
                    const hostUrl = `/meeting/host/${data.meetingId}`;
                    const clientUrl = `/meeting/client/${data.meetingId}`;

                    if (selectedMode === "host") {
                        alert("기록자로 새 회의를 시작합니다: ");
                        console.log("host > "+hostUrl);
                        window.location.href = hostUrl; // 기록자 페이지로 이동
                    } else {
                        alert("참여자로 새 회의에 참가합니다. :  " + clientUrl);
                        console.log("client > "+clientUrl);
                        window.location.href = clientUrl; // 참여자 페이지로 이동
                    }
                } else {
                    //data.success가 false일때...
                    console.log("우?");
                    alert(`회의 생성 실패: ${data.message || "알 수 없는 오류"}`);
                }
            })
            .catch(error => { //400, 500
                console.error("회의 생성 요청 실패:", error);
                if(error.message.includes("500")){
                    alert("이미 저장된 회의 URL입니다. 이어 참가하기를 통해 회의에 참여바랍니다.");
                }else{
                    alert(`네트워크 오류 또는 서버 문제로 회의 생성에 실패했습니다.\n오류 메시지: ${error.message}`);
                }
            });
        } else {
            // 이어 참가하기
            const meetingId = meetingTitleSelect.value;
            if (!meetingId) {
                alert("회의를 선택해주세요.");
                return;
            }

            const mode = document.querySelector('input[name="mode"]:checked').value;
            const userId = document.getElementById("userId").value;
            
            // 직접 리다이렉션
            window.location.href = mode === "host"
                ? `/meeting/host/${meetingId}?userId=${userId}`
                : `/meeting/client/${meetingId}?userId=${userId}`;
        }
    });

    // 탭 전환 처리
    tabs.forEach(tab => {
        tab.addEventListener("click", function() {
            tabs.forEach(t => t.classList.remove("active"));
            this.classList.add("active");

            if (this.id === "newRoom") {
                isNewMeeting = true;
                meetingForm.action = "/meeting/create";
                meetingUrlInputNew.style.display = "block";
                meetingUrlInputSelect.style.display = "none";
                meetingTitleInput.style.display = "block";
                meetingTitleSelect.style.display = "none";
                meetingTitleInput.value = "";
                meetingUrlInputNew.value = "";
            } else {
                isNewMeeting = false;
                meetingUrlInputNew.style.display = "none";
                meetingUrlInputSelect.style.display = "block";
                meetingTitleInput.style.display = "none";
                meetingTitleSelect.style.display = "block";
                loadMeetings();
            }
        });
    });
});

// 회의 목록 로드
function loadMeetings() {
    const userId = document.getElementById("userId").value;
    fetch("/meeting/user/meetingInfo", {
        method: "POST",
        headers: {
            "Content-Type": "application/json"
        },
        body: JSON.stringify({userId: userId})
    })
    .then(response => response.json())
    .then(data => {
        updateMeetingDropdown(data);
    })
    .catch(error => console.error("회의 데이터 로드 실패:", error));
}

// 드롭다운 업데이트
function updateMeetingDropdown(meetings) {
    const meetingTitleSelect = document.getElementById("meetingTitle-select");
    meetingTitleSelect.innerHTML = '<option value="">회의를 선택하세요</option>';
    meetingsData = meetings;

    meetings.forEach(meeting => {
        const option = document.createElement("option");
        option.value = meeting.meetingId;
        option.textContent = meeting.meetingName;
        meetingTitleSelect.appendChild(option);
    });
}

// 회의 선택 시 URL 업데이트
document.getElementById("meetingTitle-select").addEventListener("change", function() {
    const selectedMeeting = meetingsData.find(meeting => meeting.meetingId === Number(this.value));
    document.getElementById("meetingUrl-input-select").value = 
        selectedMeeting ? selectedMeeting.meetingUrl : "";
});