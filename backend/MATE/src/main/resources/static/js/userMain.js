    let meetingsData = [];
    let isNewMeeting = true;

    function moveMain(){
        window.location.href="/user/userMain";
    }
    document.addEventListener("DOMContentLoaded", function () {
        //탭버튼 동작
        const tabs = document.querySelectorAll(".tab-button");

        //동작변수
        const urlInputLabel = document.getElementById("meetingUrl-label");
        const meetingTitleInput = document.getElementById("meetingTitle-input");
        const meetingTitleSelect = document.getElementById("meetingTitle-select");
        const meetingUrlInputNew = document.getElementById("meetingUrl-input-new");
        const meetingUrlInputSelect = document.getElementById("meetingUrl-input-select");
        const connectButton = document.querySelector(".connect-button");
        const radioClient = document.getElementById("client");
        const radioHost = document.getElementById("host");

        const meetingTitle = document.getElementById("meetingTitle");
        const meetingUrl = document.getElementById("meetingUrl");
        const meetingCard = document.querySelector(".meeting-card");
        const meetingSelectBtn = document.getElementById("meeting-select-button");

        tabs.forEach(tab => {
            tab.addEventListener("click", function () {
                // 모든 탭에서 active 클래스 제거
                tabs.forEach(t => t.classList.remove("active"));

                // 클릭한 탭에 active 클래스 추가
                this.classList.add("active");

                // "이어 참가하기"와 "새로 참가하기"에 따른 입력 필드 변경
                if (this.id === "newRoom") {
                    isNewMeeting = true;
                    meetingUrlInputNew.value="";
                    meetingUrlInputNew.style.display = "block";
                    meetingUrlInputSelect.style.display = "none";
                    meetingTitleInput.style.display = "block"; // 입력 필드 보이기
                    meetingTitleSelect.style.display = "none"; // 셀렉트 숨기기
                    meetingCard.insertBefore(meetingUrl, meetingTitle);
                    meetingSelectBtn.style.display = "block";
                } else {
                    isNewMeeting = false;
                    meetingUrlInputSelect.value="";
                    meetingUrlInputNew.style.display = "none";
                    meetingUrlInputSelect.style.display = "block";
                    meetingTitleInput.style.display = "none"; // 입력 필드 보이기
                    meetingTitleSelect.style.display = "block"; // 셀렉트 보이기
                    meetingCard.insertBefore(meetingTitle, meetingUrl);
                    meetingSelectBtn.style.display = "none";
                    //회의 리스트 세팅
                    loadMeetings();
                }
            });
        });
    });
    //회의목록 호출
    function loadMeetings() {
        const userId = document.getElementById("userId");
        console.log(">>> userId:"+userId.value);
        //ajax 요청(javascript)
        fetch("/meeting/user/meetingInfo",{
            method:"POST",
            headers : {
                "Content-Type" : "application/json"
            },
            body : JSON.stringify({userId : userId.value})
        })
        .then(response => response.json())
        .then(data => {
            console.log("회의 정보 : ", data);
            updateMeetingDropdown(data);
        })
        .catch(error => console.error("회의 데이터 로드 실패:", error));
    }

    let checkUrl = false;
    //회의 URL 조회
    async function checkMeeting() {
        const meetingUrlInputNew = document.getElementById("meetingUrl-input-new");
        const meetingTitleInput = document.getElementById("meetingTitle-input"); // 회의 제목 입력 필드
        const selectButton = document.getElementById("meeting-select-button");
        const meetingUrl = meetingUrlInputNew.value.trim();

        console.log(">>> 입력된 회의 URL:", meetingUrl);

        if (!meetingUrl) {
            alert("회의 URL을 입력하세요.");
            return;
        }

        try {
            // 서버에 회의 URL 중복 확인 요청
            const response = await fetch('/meeting/checkMeetingUrl', {
                method: 'POST',
                headers: { 'Content-Type': 'application/json' },
                body: JSON.stringify({ meetingUrl: meetingUrl })
            });

            if (!response.ok) {
                // 409 Conflict 상태일 경우 (이미 존재하는 회의 URL)
                if (response.status === 409) {
                    const result = await response.json();
                    console.log("서버 응답 (409):", result);
                    selectButton.classList.add("checkedUrl");
                    checkUrl = true;
                    meetingTitleInput.value = result.meetingName || ""; // meetingName이 있으면 입력, 없으면 빈 값
                    console.log(result.message);
                } else {
                    // 기타 오류 상태 처리
                    const errorText = await response.text();
                    console.error("서버 오류 응답:", errorText);
                    console.log(`서버 오류 발생: ${errorText}`);
                    selectButton.classList.remove("checkedUrl");
                }
            } else {
                // 사용 가능한 URL (200 OK)
                const result = await response.text();
                console.log("서버 응답 (200 OK):", result);

                selectButton.classList.add("checkedUrl");
                checkUrl = true;
                meetingTitleInput.value = "";
                alert(result);
            }
        } catch (error) {
            console.error("URL 중복 확인 중 오류 발생:", error);
            console.error("서버와의 연결이 원활하지 않습니다. 다시 시도해주세요.");
        }
    }
    //회의 리스트 셋팅
    function updateMeetingDropdown(meetings){
        const meetingTitleSelect = document.getElementById("meetingTitle-select");

        // 기존 옵션 초기화 (첫 번째 기본 옵션 제외)
        meetingTitleSelect.innerHTML = '<option value="">회의를 선택하세요</option>';
        meetingsData = meetings;

        meetings.forEach(meeting => {
            const option = document.createElement("option");
            option.value = meeting.meetingId; // 회의 ID
            option.textContent = meeting.meetingName; // 회의제목
            meetingTitleSelect.appendChild(option);
        });

        console.log("회의 드롭다운이 업데이트되었습니다:");
    }
    //회의 선택 시 url 추가
    document.getElementById("meetingTitle-select").addEventListener("change", function() {
        const selectedValue = this.value; // 선택된 회의 ID

        // 선택된 회의 정보를 찾아서 URL을 가져옴
        const selectedMeeting = meetingsData.find(meeting => meeting.meetingId === Number(selectedValue));
        if (selectedMeeting) {
            document.getElementById("meetingUrl-input-select").value = selectedMeeting.meetingUrl;
        } else {
            console.warn("선택된 회의 정보를 찾을 수 없습니다.");
            document.getElementById("meetingUrl-input-select").value = ""; // URL 초기화
        }
    });
    //접속 버튼 클릭 시
    function connectMeeting(){
        const selectedMode = document.querySelector('input[name="mode"]:checked').value;
        const meetingUrlInputNew = document.getElementById("meetingUrl-input-new").value.trim();
        const meetingUrlInputSelect = document.getElementById("meetingUrl-input-select").value.trim();
        const meetingId = document.getElementById("meetingTitle-select").value; // 참여자가 선택한 미팅

        const meetingTitleInput = document.getElementById("meetingTitle-input").value.trim(); // 회의제목
        const meetingTitleSelect = document.getElementById("meetingUrl-input-select").value.trim();
        const userId = document.getElementById("userId").value; // 현재 사용자 ID

        const hostUrl = "";
        const clientUrl = "";

        if (isNewMeeting) {
            if(!checkUrl){
                alert("회의 URL을 먼저 조회해주세요.");
                event.preventDefault();
                return;
            }
            if (!meetingUrlInputNew) {
                alert("새 회의를 만들려면 회의 생성 URL을 입력해주세요.");
                return;
            }
            // 회의 제목, 회의URL입력확인
            if (!meetingTitleInput) {
                alert("회의 제목을 입력하세요.");
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
                    alert("이미 생성된 회의입니다.");
                }else{
                    alert(`네트워크 오류 또는 서버 문제로 회의 생성에 실패했습니다.\n오류 메시지: ${error.message}`);
                }
            });
        } else {
            //DB에서 회의 가져오기
            // 이어 참가하기
            // 회의 제목, 회의URL입력확인
            if (!meetingTitleSelect) {
                alert("회의 제목을 선택해주세요.");
                return;
            }
            const hostUrl = `/meeting/host/${meetingId}`;
            const clientUrl = `/meeting/client/${meetingId}`;
            if (selectedMode === "host"){
                alert("기록자로 회의를 이어 진행합니다.: ");
                console.log("host > "+hostUrl);
                window.location.href = hostUrl;
            }else{
                alert("참여자로 이어 참가합니다");
                console.log("client > "+clientUrl);
                window.location.href = clientUrl;
            }
        }
    }