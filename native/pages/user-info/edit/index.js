/** pages/user-info/edit/index.js **/
const request = require('../../../utils/request.js');
const app = getApp();

Page({
    data: {
        userInfo: {
            nickname: '',
            avatarUrl: '',
            fullAvatarUrl: '',
            bgImage: '',
            fullBgImage: '',
            bio: ''
        }
    },

    onLoad() {
        this.loadUserInfo();
    },

    loadUserInfo() {
        // First try to load from storage
        const storageUser = wx.getStorageSync('userInfo');
        if (storageUser) {
            this.setUserData(storageUser);
        }

        // Ideally refresh from server to get latest
        // But for now storage is primary source in this app structure
    },

    setUserData(user) {
        this.setData({
            userInfo: {
                nickname: user.nickname || '',
                avatarUrl: user.avatarUrl || '',
                fullAvatarUrl: user.avatarUrl ? app.getFullUrl(user.avatarUrl) : '/assets/images/avatar_default.png',
                bgImage: user.bgImage || '',
                fullBgImage: user.bgImage ? app.getFullUrl(user.bgImage) : '', // placeholder handled in wxml
                bio: user.bio || ''
            }
        });
    },

    onNicknameInput(e) {
        this.setData({
            'userInfo.nickname': e.detail.value
        });
    },

    onBioInput(e) {
        this.setData({
            'userInfo.bio': e.detail.value
        });
    },

    // Choose And Upload Avatar
    chooseAvatar() {
        wx.chooseMedia({
            count: 1,
            mediaType: ['image'],
            sourceType: ['album', 'camera'],
            sizeType: ['compressed'], // Prefer compressed locally
            success: (res) => {
                const tempFilePath = res.tempFiles[0].tempFilePath;
                this.compressAndUpload(tempFilePath, 'avatar');
            }
        });
    },

    // Choose And Upload Background
    chooseBgImage() {
        wx.chooseMedia({
            count: 1,
            mediaType: ['image'],
            sourceType: ['album', 'camera'],
            sizeType: ['compressed'],
            success: (res) => {
                const tempFilePath = res.tempFiles[0].tempFilePath;
                this.compressAndUpload(tempFilePath, 'bg');
            }
        });
    },

    // Compress Image Helper
    compressAndUpload(src, type) {
        wx.showLoading({ title: '处理中...' });
        wx.compressImage({
            src: src,
            quality: 60, // Compress quality (0-100)
            success: (res) => {
                console.log('Compression success:', res.tempFilePath);
                this.uploadFile(res.tempFilePath, type);
            },
            fail: (err) => {
                console.error('Compression failed, using original:', err);
                this.uploadFile(src, type);
            }
        });
    },

    uploadFile(filePath, type) {
        wx.showLoading({ title: '上传中...' });

        // Use apiBaseUrl correctly
        const url = app.globalData.apiBaseUrl + '/file/upload';
        console.log('Uploading file to:', url);
        console.log('File path:', filePath);

        const token = wx.getStorageSync('token');
        const authHeader = token ? (token.startsWith('Bearer ') ? token : 'Bearer ' + token) : '';

        wx.uploadFile({
            url: url,
            filePath: filePath,
            name: 'file',
            header: {
                'Authorization': authHeader,
                'token': token
            },
            success: (res) => {
                wx.hideLoading();
                console.log('Upload success response:', res);
                try {
                    // wx.uploadFile returns data as String
                    const data = JSON.parse(res.data);
                    if (data.code === 0) {
                        const fileUrl = data.data;
                        const fullUrl = app.getFullUrl(fileUrl);

                        if (type === 'avatar') {
                            this.setData({
                                'userInfo.avatarUrl': fileUrl,
                                'userInfo.fullAvatarUrl': fullUrl
                            });
                        } else if (type === 'bg') {
                            this.setData({
                                'userInfo.bgImage': fileUrl,
                                'userInfo.fullBgImage': fullUrl
                            });
                        }
                    } else {
                        wx.showToast({ title: data.msg || '上传失败', icon: 'none' });
                        console.error('Upload failed with code:', data.code, data.msg);
                    }
                } catch (e) {
                    wx.showToast({ title: '解析失败', icon: 'none' });
                    console.error('JSON parse error:', e, res.data);
                }
            },
            fail: (e) => {
                wx.hideLoading();
                wx.showToast({ title: '网络错误: ' + e.errMsg, icon: 'none' });
                console.error('Network error during upload:', e);
            }
        });
    },

    async saveUserInfo() {
        const { nickname, avatarUrl, bgImage, bio } = this.data.userInfo;

        if (!nickname || !nickname.trim()) {
            wx.showToast({ title: '昵称不能为空', icon: 'none' });
            return;
        }

        wx.showLoading({ title: '保存中...' });
        try {
            const res = await request.put('/user/update', {
                nickname,
                avatarUrl,
                bgImage,
                bio
            });

            wx.hideLoading();
            if (res.code === 0) {
                wx.showToast({ title: '保存成功' });

                // Update Storage
                const oldUser = wx.getStorageSync('userInfo') || {};
                const newUser = {
                    ...oldUser,
                    nickname,
                    avatarUrl,
                    bgImage,
                    bio
                };
                wx.setStorageSync('userInfo', newUser);

                // Delay back
                setTimeout(() => {
                    wx.navigateBack();
                }, 1500);

            } else {
                wx.showToast({ title: res.msg || '保存失败', icon: 'none' });
            }
        } catch (e) {
            wx.hideLoading();
            wx.showToast({ title: '保存出错', icon: 'none' });
            console.error(e);
        }
    }
});
