/** pages/login/index.js **/
const request = require('../../utils/request.js');
const app = getApp();

Page({
    data: {
        avatarUrl: '',
        nickname: '',
        isLoggingIn: false,
        showAuthModal: false
    },

    onLoad() {
        // Auto-login to establish session/token
        this.doSilentLogin();
    },

    async doSilentLogin() {
        try {
            const loginRes = await new Promise((resolve, reject) => {
                wx.login({
                    success: resolve,
                    fail: reject
                });
            });

            if (loginRes.code) {
                const res = await request.post('/user/login', { code: loginRes.code }, { hideLoading: true });
                if (res.code === 0) {
                    wx.setStorageSync('token', res.data.token);
                    wx.setStorageSync('userInfo', res.data);

                    // Pre-fill if user already has info
                    if (res.data.nickname && res.data.nickname !== '微信用户') {
                        this.setData({
                            nickname: res.data.nickname || '',
                            avatarUrl: app.getFullUrl(res.data.avatarUrl) || ''
                        });
                    }
                }
            }
        } catch (e) {
            console.error('Silent login failed:', e);
        }
    },

    /**
     * Choose Avatar
     */
    onChooseAvatar(e) {
        const { avatarUrl } = e.detail;
        this.setData({ avatarUrl });
        this.uploadAvatar(avatarUrl);
    },

    /**
     * Upload Avatar
     */
    uploadAvatar(filePath) {
        if (!wx.getStorageSync('token')) {
            // Try explicit login just in case
            this.doSilentLogin().then(() => {
                if (wx.getStorageSync('token')) {
                    this._doUpload(filePath);
                } else {
                    wx.showToast({ title: '网络连接失败，请重试', icon: 'none' });
                }
            });
            return;
        }
        this._doUpload(filePath);
    },

    _doUpload(filePath) {
        wx.showLoading({ title: '上传头像中...' });
        request.uploadFile(filePath).then(url => {
            wx.hideLoading();
            // Ensure we use the full URL for display, bypassing relative path issues
            this.setData({ avatarUrl: app.getFullUrl(url) });
        }).catch(err => {
            wx.hideLoading();
            wx.showToast({ title: '上传失败', icon: 'none' });
            console.error(err);
            // Optionally revert to temp file path or keep it to let user retry
        });
    },

    onNicknameBlur(e) {
        this.setData({ nickname: e.detail.value });
    },

    onNicknameInput(e) {
        this.setData({ nickname: e.detail.value });
    },

    showAuthPopup() {
        this.setData({ showAuthModal: true });
    },

    hideAuthPopup() {
        this.setData({ showAuthModal: false });
    },

    /**
     * Finish Login (Save Profile & Enter)
     */
    async handleFinishLogin() {
        const { nickname, avatarUrl, isLoggingIn } = this.data;

        if (isLoggingIn) return;

        if (!nickname) {
            wx.showToast({ title: '请输入昵称', icon: 'none' });
            return;
        }
        if (!avatarUrl) {
            wx.showToast({ title: '请设置头像', icon: 'none' });
            return;
        }

        // Validate Avatar Upload Status (check if it's still a temp local path or raw blob)
        // If it was successfully uploaded, it should ideally be a full URL now (via _doUpload->getFullUrl).
        // However, if user just selected and upload failed or is pending, it might be temp path.
        // But logic says we upload immediately on choose.
        // Wait, onChooseAvatar calls setData(temp) then upload.
        // upload calls setData(fullUrl) on success.
        // So if we are here and it's temp, it means upload hasn't finished.
        if (avatarUrl.startsWith('wxfile://') || avatarUrl.startsWith('http://tmp') || avatarUrl.startsWith('blob:')) {
            wx.showToast({ title: '头像正在上传，请稍后...', icon: 'none' });
            return;
        }

        this.setData({ isLoggingIn: true });
        wx.showLoading({ title: '正在进入...' });

        try {
            // Ensure we have a token (in case silent login failed earlier)
            let token = wx.getStorageSync('token');
            if (!token) {
                await this.doSilentLogin();
                token = wx.getStorageSync('token');
                if (!token) throw new Error('登录失败，请检查网络');
            }

            // For update, we want to send the RELATIVE path usually, or the backend handles full URL?
            // Usually backend expects relative path to save in DB.
            // But let's check what uploadFile returns. It returns `data.data` from backend.
            // If `data.data` is relative, and we display full URL using `app.getFullUrl()`.
            // Then `this.data.avatarUrl` is currently the FULL URL.
            // If we send FULL URL back to `update`, the backend might save the full URL. This is generally OK but can cause double-domain issues if concatenated later.
            // Ideally we should strip the domain if saving.
            // BUT, `request.put` sends JSON.

            // Let's rely on backend being smart enough or just send what we have.
            // For safety, let's just send it. If backend saves full URL, subsequent getFullUrl checks `startsWith('http')` and returns it as is. So it's idempotent.

            const res = await request.put('/user/update', {
                nickname: nickname,
                avatarUrl: avatarUrl
            });

            wx.hideLoading();

            if (res.code === 0) {
                // Update Storage
                const user = wx.getStorageSync('userInfo') || {};
                user.nickname = nickname;
                user.avatarUrl = avatarUrl;
                wx.setStorageSync('userInfo', user);

                wx.showToast({ title: '欢迎回来', icon: 'success' });
                setTimeout(() => {
                    // Check if there is a redirect URL
                    const pages = getCurrentPages();
                    if (pages.length > 1) {
                        wx.navigateBack();
                    } else {
                        wx.switchTab({ url: '/pages/index/index' });
                    }
                }, 1000);
            } else {
                wx.showToast({ title: res.msg || '保存失败', icon: 'none' });
                this.setData({ isLoggingIn: false });
            }

        } catch (e) {
            wx.hideLoading();
            console.error(e);
            wx.showToast({ title: e.message || '操作失败', icon: 'none' });
            this.setData({ isLoggingIn: false });
        }
    }
})
