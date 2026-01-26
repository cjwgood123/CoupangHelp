// 판매실적추이 그래프 모달 관련 함수들

// 판매실적추이 그래프 모달 관련 함수
function showSalesChart() {
    const modal = document.getElementById('salesChartModal');
    if (modal) {
        modal.classList.add('show');
        checkLoginStatus();
    }
}

function closeSalesChart() {
    const modal = document.getElementById('salesChartModal');
    if (modal) {
        modal.classList.remove('show');
    }
}

// 모달 외부 클릭 시 닫기
document.addEventListener('DOMContentLoaded', function() {
    const modal = document.getElementById('salesChartModal');
    if (modal) {
        modal.addEventListener('click', function(e) {
            if (e.target === this) {
                closeSalesChart();
            }
        });
    }
});

// 로그인 상태 확인
function checkLoginStatus() {
    const userId = localStorage.getItem('salesChart_userId');
    const loginSection = document.getElementById('loginSection');
    const dataInputSection = document.getElementById('dataInputSection');
    const userInfoText = document.getElementById('userInfoText');

    if (!loginSection || !dataInputSection || !userInfoText) return;

    if (userId) {
        loginSection.style.display = 'none';
        dataInputSection.classList.add('show');
        userInfoText.textContent = '사용자: ' + userId;
    } else {
        loginSection.style.display = 'block';
        dataInputSection.classList.remove('show');
        showLoginForm();
    }
}

// 로그인 폼 표시
function showLoginForm() {
    const loginForm = document.getElementById('loginForm');
    const signupForm = document.getElementById('signupForm');
    if (loginForm) loginForm.style.display = 'block';
    if (signupForm) signupForm.style.display = 'none';
}

// 회원가입 폼 표시
function showSignupForm() {
    showWarningAlert();
    const loginForm = document.getElementById('loginForm');
    const signupForm = document.getElementById('signupForm');
    if (loginForm) loginForm.style.display = 'none';
    if (signupForm) signupForm.style.display = 'block';
}

// 셀러 옵션 변경 시 기타 입력란 표시/숨김
function toggleSellerOther() {
    const sellerTypeRadio = document.querySelector('input[name="sellerType"]:checked');
    if (!sellerTypeRadio) return;
    
    const sellerType = sellerTypeRadio.value;
    const sellerOtherGroup = document.getElementById('sellerOtherGroup');
    const sellerOther = document.getElementById('sellerOther');
    
    if (sellerOtherGroup && sellerOther) {
        if (sellerType === 'OTHER') {
            sellerOtherGroup.style.display = 'block';
            sellerOther.required = true;
        } else {
            sellerOtherGroup.style.display = 'none';
            sellerOther.required = false;
            sellerOther.value = '';
        }
    }
}

// 경고 알림 표시
function showWarningAlert() {
    alert('개인정보를 따로 저장하지 않습니다.\nID/PW 찾기 시 관리자에게 문의하셔야 합니다.');
}

// 회원가입
async function handleSignup() {
    const userId = document.getElementById('signupUserId')?.value.trim();
    const userPw = document.getElementById('signupUserPw')?.value.trim();
    const userPwConfirm = document.getElementById('signupUserPwConfirm')?.value.trim();
    const sellerTypeRadio = document.querySelector('input[name="sellerType"]:checked');
    const sellerOther = document.getElementById('sellerOther')?.value.trim();

    if (!userId || !userPw || !userPwConfirm) {
        alert('모든 필드를 입력해주세요.');
        return;
    }

    if (!sellerTypeRadio) {
        alert('셀러 옵션을 선택해주세요.');
        return;
    }

    const sellerType = sellerTypeRadio.value;

    if (userPw !== userPwConfirm) {
        alert('비밀번호가 일치하지 않습니다.');
        return;
    }

    if (sellerType === 'OTHER' && !sellerOther) {
        alert('기타 셀러명을 입력해주세요.');
        return;
    }

    try {
        const response = await fetch('/api/sales-chart/signup', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: userId,
                userPw: userPw,
                userPwConfirm: userPwConfirm,
                sellerType: sellerType,
                sellerOther: sellerType === 'OTHER' ? sellerOther : null
            })
        });

        const result = await response.json();
        
        if (result.success) {
            alert('회원가입이 완료되었습니다. 자동으로 로그인됩니다.');
            // 자동 로그인
            localStorage.setItem('salesChart_userId', result.userId);
            // 회원가입 성공 시 신규 페이지로 리다이렉트
            window.location.href = '/sales-chart';
        } else {
            alert(result.message || '회원가입에 실패했습니다.');
        }
    } catch (error) {
        console.error('회원가입 오류:', error);
        alert('회원가입 중 오류가 발생했습니다. 다시 시도해주세요.');
    }
}

// 로그인
async function handleLogin() {
    const userId = document.getElementById('userId')?.value.trim();
    const userPw = document.getElementById('userPw')?.value.trim();

    if (!userId || !userPw) {
        alert('아이디와 비밀번호를 모두 입력해주세요.');
        return;
    }

    try {
        const response = await fetch('/api/sales-chart/login', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({
                userId: userId,
                userPw: userPw
            })
        });

        const result = await response.json();
        
        if (result.success) {
            localStorage.setItem('salesChart_userId', result.userId);
            // 로그인 성공 시 신규 페이지로 리다이렉트
            window.location.href = '/sales-chart';
        } else {
            alert(result.message || '로그인에 실패했습니다.');
        }
    } catch (error) {
        console.error('로그인 오류:', error);
        alert('로그인 중 오류가 발생했습니다. 다시 시도해주세요.');
    }
}

// 로그아웃
function handleLogout() {
    if (confirm('로그아웃 하시겠습니까?')) {
        localStorage.removeItem('salesChart_userId');
        checkLoginStatus();
    }
}

// 데이터 파싱 및 미리보기
function parseAndPreview() {
    const extractDate = document.getElementById('extractDate')?.value.trim();
    const productNumber = document.getElementById('productNumber')?.value.trim();
    const dataPaste = document.getElementById('dataPaste')?.value.trim();

    if (!extractDate || !productNumber) {
        alert('데이터 추출일자와 상품번호를 입력해주세요.');
        return;
    }

    if (!dataPaste) {
        alert('시간별 판매 데이터를 입력해주세요.');
        return;
    }

    const lines = dataPaste.split('\n').filter(line => line.trim());
    const data = [];
    
    for (let line of lines) {
        // 탭이나 여러 공백으로 구분된 데이터 파싱
        const parts = line.trim().split(/\s+/);
        if (parts.length >= 3) {
            const time = parts[0];
            const sales = parts[1];
            const revenue = parts[2];
            
            // 숫자 형식 검증
            if (time && !isNaN(parseFloat(sales)) && !isNaN(parseFloat(revenue))) {
                data.push({
                    time: time,
                    sales: parseFloat(sales),
                    revenue: parseInt(revenue)
                });
            }
        }
    }

    if (data.length === 0) {
        alert('올바른 형식의 데이터를 입력해주세요.\n예: 00h   0.00   0');
        return;
    }

    // 미리보기 테이블 생성
    const tbody = document.getElementById('previewTableBody');
    if (tbody) {
        tbody.innerHTML = '';
        
        data.forEach(item => {
            const row = document.createElement('tr');
            row.innerHTML = `
                <td>${item.time}</td>
                <td>${item.sales.toFixed(2)}</td>
                <td>${item.revenue.toLocaleString()}</td>
            `;
            tbody.appendChild(row);
        });

        const preview = document.getElementById('dataPreview');
        if (preview) preview.style.display = 'block';
    }
}

// 데이터 저장
function saveData() {
    const extractDate = document.getElementById('extractDate')?.value.trim();
    const productNumber = document.getElementById('productNumber')?.value.trim();
    const dataPaste = document.getElementById('dataPaste')?.value.trim();

    if (!extractDate || !productNumber || !dataPaste) {
        alert('모든 필드를 입력해주세요.');
        return;
    }

    // 데이터 파싱
    const lines = dataPaste.split('\n').filter(line => line.trim());
    const data = [];
    
    for (let line of lines) {
        const parts = line.trim().split(/\s+/);
        if (parts.length >= 3) {
            const time = parts[0];
            const sales = parts[1];
            const revenue = parts[2];
            
            if (time && !isNaN(parseFloat(sales)) && !isNaN(parseFloat(revenue))) {
                data.push({
                    time: time,
                    sales: parseFloat(sales),
                    revenue: parseInt(revenue)
                });
            }
        }
    }

    if (data.length === 0) {
        alert('올바른 형식의 데이터를 입력해주세요.');
        return;
    }

    // 로컬스토리지에 저장 (테이블은 나중에 만들 예정)
    const userId = localStorage.getItem('salesChart_userId');
    const salesData = {
        extractDate: extractDate,
        productNumber: productNumber,
        data: data,
        savedAt: new Date().toISOString()
    };

    const userData = JSON.parse(localStorage.getItem('salesChart_data_' + userId) || '[]');
    userData.push(salesData);
    localStorage.setItem('salesChart_data_' + userId, JSON.stringify(userData));

    alert('데이터가 저장되었습니다.\n(현재는 로컬스토리지에만 저장되며, 테이블은 추후 구현 예정입니다.)');
    
    // 입력 필드 초기화
    if (document.getElementById('extractDate')) document.getElementById('extractDate').value = '';
    if (document.getElementById('productNumber')) document.getElementById('productNumber').value = '';
    if (document.getElementById('dataPaste')) document.getElementById('dataPaste').value = '';
    const preview = document.getElementById('dataPreview');
    if (preview) preview.style.display = 'none';
}

