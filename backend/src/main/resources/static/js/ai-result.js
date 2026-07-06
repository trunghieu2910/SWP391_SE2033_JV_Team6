// Parse URL parameter
const urlParams = new URLSearchParams(window.location.search);
const imageId = urlParams.get('imageId');

document.addEventListener('DOMContentLoaded', () => {
    if (!imageId) {
        alert("Không tìm thấy ID hình ảnh!");
        return;
    }
    
    document.getElementById('image-id').textContent = '#MI' + imageId;
    fetchImageDetails(imageId);
});

async function fetchImageDetails(id) {
    try {
        // Gọi API lấy dữ liệu chi tiết ảnh từ Java
        const response = await fetch(`/api/medical-images/${id}`);
        if (response.ok) {
            const data = await response.json();
            
            document.getElementById('image-type').textContent = data.imageType || 'Siêu âm';
            
            // Orig image
            const origImg = document.getElementById('original-img');
            if (data.imageUrl) {
                origImg.src = data.imageUrl;
                origImg.classList.remove('placeholder');
            }
            
            // AI Image
            if (data.aiImageUrl) {
                const aiImg = document.getElementById('ai-img');
                aiImg.src = data.aiImageUrl;
                aiImg.classList.remove('placeholder');
                
                document.getElementById('ai-status').textContent = 'HOÀN THÀNH';
                document.querySelector('.status-pill').className = 'status-pill success';
                
                if (data.confidenceScore) {
                    document.getElementById('ai-confidence').textContent = (data.confidenceScore * 100).toFixed(2) + '%';
                }
                
                document.getElementById('btn-process-ai').style.display = 'none';
            } else {
                document.getElementById('ai-status').textContent = 'CHƯA PHÂN TÍCH';
                document.querySelector('.status-pill').className = 'status-pill pending';
            }
        } else {
            console.error("Lỗi khi lấy dữ liệu ảnh");
        }
    } catch (e) {
        console.error(e);
    }
}

async function processAI() {
    if (!imageId) return;
    
    const btn = document.getElementById('btn-process-ai');
    const loadingAi = document.getElementById('loading-ai');
    
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang xử lý...';
    loadingAi.style.display = 'flex';
    
    try {
        // API endpoint để gọi Java -> Python AI
        const response = await fetch(`/api/ultrasound/process-ai/${imageId}`, {
            method: 'POST'
        });
        
        if (response.ok) {
            const data = await response.json();
            
            const aiImg = document.getElementById('ai-img');
            aiImg.src = data.aiImageUrl;
            aiImg.classList.remove('placeholder');
            
            document.getElementById('ai-status').textContent = 'HOÀN THÀNH';
            document.querySelector('.status-pill').className = 'status-pill success';
            
            if (data.confidenceScore) {
                document.getElementById('ai-confidence').textContent = (data.confidenceScore * 100).toFixed(2) + '%';
            }
            
            btn.style.display = 'none';
        } else {
            alert('Lỗi xử lý AI: ' + await response.text());
        }
    } catch (e) {
        alert('Không thể kết nối đến server');
        console.error(e);
    } finally {
        loadingAi.style.display = 'none';
        btn.disabled = false;
        btn.innerHTML = '<i class="fa-solid fa-robot"></i> Chạy Phân Tích AI';
    }
}
