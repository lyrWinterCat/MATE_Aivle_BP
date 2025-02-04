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
            
            // mode 값 설정
            document.getElementById('selectedMode').value = 
                document.querySelector('input[name="mode"]:checked').value;
                
            this.submit();
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