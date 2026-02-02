const request = require('../../../utils/request.js');
const app = getApp();

Page({
    data: {
        headerSpacerHeight: 0,
        headerTotalHeight: 0,

        pageTitle: '发布动态',
        type: 3,

        categories: ['日常吐槽', '表白墙', '求助问答', '学习交流', '其他话题'],

        formData: {
            title: '',
            content: '',
            category: '',
            tags: '',
            location: '',
            isAnonymous: 0,
            status: 0
        },
        localImages: [],
        tagList: [],

        showTagModal: false,
        tempTag: '',
        showPreview: false,
        userInfo: { nickname: '我', avatar: '' },

        publishing: false,
        isEditMode: false,
        editId: null
    },

    onLoad(options) {
        this.calcHeaderHeight();
        if (options.type) {
            this.setData({ type: parseInt(options.type) });
            if (parseInt(options.type) === 3) {
                // Square
                this.setData({ pageTitle: '发布校园广场' });
            }
        }

        // Defaults
        const currentLoc = wx.getStorageSync('selectedUniversity') || '浙江大学';
        this.setData({
            ['formData.location']: currentLoc
        });

        // Load userInfo early for preview
        const u = wx.getStorageSync('userInfo');
        if (u) this.setData({ userInfo: u });

        // Edit Mode or Drafts
        if (options.id && options.mode === 'edit') {
            this.setData({
                isEditMode: true,
                editId: options.id,
                pageTitle: this.data.type === 3 ? '编辑动态' : '编辑动态'
            });
            this.loadDetail(options.id);
        } else {
            const draft = wx.getStorageSync('post_draft_' + this.data.type);
            if (draft) {
                wx.showModal({
                    title: '提示',
                    content: '检测到有未发布的草稿，是否恢复？',
                    success: (res) => {
                        if (res.confirm) {
                            const data = JSON.parse(draft);
                            const tags = (data.tags) ? data.tags.split(',').filter(t => t) : [];
                            // Map draft data to formData
                            this.setData({
                                formData: {
                                    title: data.title || '',
                                    content: data.content || '',
                                    category: data.category || '',
                                    tags: data.tags || '',
                                    location: data.location || currentLoc,
                                    isAnonymous: data.isAnonymous || 0
                                },
                                localImages: data.localImages || [],
                                tagList: tags
                            });
                        } else {
                            wx.removeStorageSync('post_draft_' + this.data.type);
                        }
                    }
                });
            }
        }
    },

    calcHeaderHeight() {
        const sysInfo = wx.getSystemInfoSync();
        let top = sysInfo.statusBarHeight;
        let spacerH = top + 44;
        const menuButton = wx.getMenuButtonBoundingClientRect();
        if (menuButton) { spacerH = menuButton.bottom + 8; }
        this.setData({ headerSpacerHeight: spacerH, headerTotalHeight: spacerH + 50 });
    },

    async loadDetail(id) {
        try {
            wx.showLoading({ title: '加载中' });
            const res = await request.get('/post/' + id);
            wx.hideLoading();
            if (res.code === 0 && res.data) {
                const data = res.data;
                const tags = data.tags ? data.tags.split(',').filter(t => t) : [];
                // Process images - convert relative paths to full URLs for edit mode
                let localImages = [];
                if (data.imageUrls) {
                    const urls = data.imageUrls.split(',').filter(u => u.trim());
                    localImages = urls.map(u => app.getFullUrl(u.trim()));
                }
                this.setData({
                    formData: {
                        title: data.title || '',
                        content: data.content || '',
                        category: data.category || '',
                        tags: data.tags || '',
                        location: data.location || '',
                        isAnonymous: data.isAnonymous || 0,
                        status: data.status != null ? data.status : 0
                    },
                    localImages: localImages,
                    tagList: tags
                });
            }
        } catch (e) {
            wx.hideLoading();
        }
    },

    goBack() {
        if (!this.data.isEditMode && (this.data.formData.content || this.data.localImages.length > 0)) {
            wx.showModal({
                title: '提示',
                content: '是否保存草稿？',
                cancelText: '不保存',
                confirmText: '保存',
                success: (res) => {
                    if (res.confirm) {
                        wx.setStorageSync('post_draft_' + this.data.type, JSON.stringify({
                            ...this.data.formData,
                            localImages: this.data.localImages
                        }));
                        wx.showToast({ title: '草稿已保存', icon: 'none' });
                    }
                    wx.navigateBack();
                }
            });
        } else {
            wx.navigateBack();
        }
    },

    // Inputs
    onTitleInput(e) { this.setData({ ['formData.title']: e.detail.value }); },
    onContentInput(e) { this.setData({ ['formData.content']: e.detail.value }); },
    onCategoryChange(e) { this.setData({ ['formData.category']: this.data.categories[e.detail.value] }); },
    onAnonymousChange(e) { this.setData({ ['formData.isAnonymous']: e.detail.value ? 1 : 0 }); },
    onStatusChange(e) { this.setData({ ['formData.status']: e.detail.value ? 0 : 1 }); }, // 0=On shelf/active, 1=Off shelf/inactive

    // Tags
    showTagInput() { this.setData({ showTagModal: true, tempTag: '' }); },
    closeTagModal() { this.setData({ showTagModal: false }); },
    onTagInput(e) { this.setData({ tempTag: e.detail.value }); },
    confirmTag() {
        const tag = this.data.tempTag.trim();
        if (tag) {
            const tags = this.data.tagList;
            if (tags.length >= 5) {
                return wx.showToast({ title: '最多5个标签', icon: 'none' });
            }
            if (tags.includes(tag)) {
                return wx.showToast({ title: '标签已存在', icon: 'none' });
            }
            tags.push(tag);
            this.setData({
                tagList: tags,
                ['formData.tags']: tags.join(','),
                showTagModal: false
            });
        } else {
            this.setData({ showTagModal: false });
        }
    },

    // Images
    chooseImage() {
        wx.chooseMedia({
            count: 9 - this.data.localImages.length,
            mediaType: ['image'],
            success: (res) => {
                const paths = res.tempFiles.map(f => f.tempFilePath);
                this.setData({ localImages: [...this.data.localImages, ...paths] });
            }
        });
    },
    deleteImage(e) {
        const i = e.currentTarget.dataset.index;
        const list = this.data.localImages;
        list.splice(i, 1);
        this.setData({ localImages: list });
    },
    previewImage(e) {
        wx.previewImage({ urls: this.data.localImages, current: this.data.localImages[e.currentTarget.dataset.index] });
    },

    // Preview
    onPreview() {
        if (!this.data.formData.content && this.data.localImages.length === 0) {
            return wx.showToast({ title: '写点什么吧', icon: 'none' });
        }
        this.setData({ showPreview: true });
    },
    closePreview() { this.setData({ showPreview: false }); },
    stopProp() { },

    async submit() {
        if (this.data.publishing) return;
        const { formData, localImages, type } = this.data;

        if (!formData.content.trim() && localImages.length === 0) {
            return wx.showToast({ title: '内容不能为空', icon: 'none' });
        }
        if (!formData.category) {
            return wx.showToast({ title: '请选择分类', icon: 'none' });
        }

        this.setData({ publishing: true });
        wx.showLoading({ title: '发布中...' });

        try {
            let uploadedUrls = [];

            // 1. Upload Images
            if (localImages.length > 0) {
                for (let path of localImages) {
                    const isRemote = (path.startsWith('http') || path.startsWith('https')) && !path.includes('http://tmp') && !path.includes('wxfile');
                    if (isRemote) {
                        // Extract relative path from full URL
                        let relativePath = path;
                        const profileIndex = path.indexOf('/profile/');
                        if (profileIndex !== -1) {
                            relativePath = path.substring(profileIndex);
                        }
                        uploadedUrls.push(relativePath);
                    } else {
                        const url = await request.uploadFile(path);
                        if (url) uploadedUrls.push(url);
                    }
                }
            }

            // 2. Build Payload
            const postData = {
                title: formData.title || '',
                content: formData.content,
                category: formData.category,
                tags: formData.tags,
                location: formData.location,
                isAnonymous: formData.isAnonymous,
                status: formData.status,
                type: type,
                imageUrls: uploadedUrls.join(',')
            };

            // 3. Send
            if (this.data.isEditMode) {
                postData.id = parseInt(this.data.editId);
                await request.put('/post/update', postData);
            } else {
                await request.post('/post/add', postData);
            }

            wx.hideLoading();
            wx.showToast({ title: '发布成功', icon: 'success' });

            // Clean up
            wx.removeStorageSync('post_draft_' + type);

            const pages = getCurrentPages();
            const prevPage = pages[pages.length - 2];
            if (prevPage && prevPage.refreshList) { prevPage.refreshList(); }

            this.setData({ showPreview: false });

            setTimeout(() => { wx.navigateBack(); }, 1500);

        } catch (e) {
            console.error(e);
            wx.hideLoading();
            this.setData({ publishing: false });
            // Error handling usually handled by request.js, but fallback:
            if (e && e.msg) wx.showToast({ title: e.msg, icon: 'none' });
            else wx.showToast({ title: '发布失败', icon: 'none' });
        }
    }
});
