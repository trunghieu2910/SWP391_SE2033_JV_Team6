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
            
            // Manual Image
            if (data.imgResultConclusion) {
                const manualImgBox = document.getElementById('manual-img-box');
                const manualImg = document.getElementById('manual-img');
                if (manualImgBox && manualImg) {
                    manualImgBox.style.display = 'block';
                    manualImg.src = data.imgResultConclusion;
                    manualImg.classList.remove('placeholder');
                }
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
            
            if (data.ultrasoundConclusion) {
                // For Ultrasound Doctor Role input (if it's not hidden, but we will hide the whole block)
                const ta = document.getElementById('ultrasound-conclusion');
                if (ta) ta.value = data.ultrasoundConclusion;
                
                // Hide the editing controls since it's already saved
                const techControls = document.querySelector('.ultrasound-controls');
                if (techControls) {
                    techControls.style.display = 'none';
                }
                
                // For Doctor/Patient/History view (show static text)
                const tcView = document.getElementById('ultrasound-conclusion-view');
                const tcText = document.getElementById('ultrasound-conclusion-text');
                if (tcView && tcText) {
                    tcView.style.display = 'block';
                    tcText.textContent = data.ultrasoundConclusion;
                }
            }
            
            if (data.imgResultConclusion) {
                const manualImgBox = document.getElementById('manual-img-box');
                const manualImg = document.getElementById('manual-img');
                if (manualImgBox && manualImg) {
                    manualImgBox.style.display = 'block';
                    manualImg.src = data.imgResultConclusion;
                    manualImg.classList.remove('placeholder');
                }
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

// ================= MANUAL DRAWING LOGIC =================
let isDrawingMode = false;
let isDrawing = false;
let startX = 0, startY = 0;
let drawCanvas = null, ctx = null;
let hasDrawn = false;
let drawnRects = []; // Store drawn rectangles to redraw on clear/resize

function initCanvas() {
    const img = document.getElementById('original-img');
    drawCanvas = document.getElementById('draw-canvas');
    if (!drawCanvas) return;
    
    // Resize canvas to match image display size
    drawCanvas.width = img.clientWidth;
    drawCanvas.height = img.clientHeight;
    ctx = drawCanvas.getContext('2d');
    
    // Set styles
    ctx.strokeStyle = 'red';
    ctx.lineWidth = 3;
    
    // Events
    drawCanvas.onmousedown = (e) => {
        isDrawing = true;
        const rect = drawCanvas.getBoundingClientRect();
        startX = e.clientX - rect.left;
        startY = e.clientY - rect.top;
    };
    
    drawCanvas.onmousemove = (e) => {
        if (!isDrawing) return;
        const rect = drawCanvas.getBoundingClientRect();
        const currentX = e.clientX - rect.left;
        const currentY = e.clientY - rect.top;
        
        ctx.clearRect(0, 0, drawCanvas.width, drawCanvas.height);
        
        // Redraw old rects
        drawnRects.forEach(r => {
            ctx.strokeRect(r.x, r.y, r.w, r.h);
        });
        
        // Draw current
        ctx.strokeRect(startX, startY, currentX - startX, currentY - startY);
    };
    
    drawCanvas.onmouseup = (e) => {
        if (!isDrawing) return;
        isDrawing = false;
        hasDrawn = true;
        
        const rect = drawCanvas.getBoundingClientRect();
        const currentX = e.clientX - rect.left;
        const currentY = e.clientY - rect.top;
        drawnRects.push({
            x: startX, y: startY, w: currentX - startX, h: currentY - startY
        });
    };
}

function toggleManualDraw() {
    const btn = document.getElementById('btn-manual-draw');
    drawCanvas = document.getElementById('draw-canvas');
    if (!drawCanvas) return;
    
    isDrawingMode = !isDrawingMode;
    if (isDrawingMode) {
        initCanvas();
        drawCanvas.style.display = 'block';
        btn.style.background = '#dc2626';
        btn.innerHTML = '<i class="fa-solid fa-xmark"></i> Hủy vẽ tay';
    } else {
        drawCanvas.style.display = 'none';
        btn.style.background = '#f59e0b';
        btn.innerHTML = '<i class="fa-solid fa-pen"></i> Khoanh vùng thủ công';
        // Reset
        if (ctx) ctx.clearRect(0, 0, drawCanvas.width, drawCanvas.height);
        drawnRects = [];
        hasDrawn = false;
    }
}

async function saveUltrasoundConclusion() {
    const conclusion = document.getElementById('ultrasound-conclusion')?.value || '';
    
    const btn = document.getElementById('btn-save-conclusion');
    btn.disabled = true;
    btn.innerHTML = '<i class="fa-solid fa-spinner fa-spin"></i> Đang lưu...';
    
    let manualImageBase64 = '';
    
    if (hasDrawn && isDrawingMode) {
        // Create a temporary canvas to combine image and drawing
        const img = document.getElementById('original-img');
        const tempCanvas = document.createElement('canvas');
        tempCanvas.width = img.naturalWidth;
        tempCanvas.height = img.naturalHeight;
        const tCtx = tempCanvas.getContext('2d');
        
        // Draw original image
        tCtx.drawImage(img, 0, 0);
        
        // Scale and draw rects to match natural width
        const scaleX = img.naturalWidth / drawCanvas.width;
        const scaleY = img.naturalHeight / drawCanvas.height;
        
        tCtx.strokeStyle = 'red';
        tCtx.lineWidth = 3 * Math.max(scaleX, scaleY);
        drawnRects.forEach(r => {
            tCtx.strokeRect(r.x * scaleX, r.y * scaleY, r.w * scaleX, r.h * scaleY);
        });
        
        manualImageBase64 = tempCanvas.toDataURL('image/jpeg', 0.9);
    }
    
    const errorDiv = document.getElementById('conclusion-error');
    if (errorDiv) errorDiv.style.display = 'none';

    try {
        const payload = { conclusion, manualImageBase64 };
        const response = await fetch(`/api/ultrasound/save-conclusion/${imageId}`, {
            method: 'POST',
            headers: { 'Content-Type': 'application/json' },
            body: JSON.stringify(payload)
        });
        
        if (response.ok) {
            window.location.href = '/ultrasound-doctor/dashboard';
        } else {
            const errorMsg = await response.text();
            if (errorDiv) {
                errorDiv.querySelector('span').textContent = errorMsg;
                errorDiv.style.display = 'block';
            } else {
                alert('Lỗi: ' + errorMsg);
            }
        }
    } catch (e) {
        alert('Lỗi kết nối máy chủ');
        console.error(e);
    } finally {
        btn.disabled = false;
        btn.innerHTML = '<i class="fa-solid fa-save"></i> Lưu Kết Luận';
    }
}
