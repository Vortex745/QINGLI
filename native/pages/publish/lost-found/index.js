const request = require('../../../utils/request.js');
const app = getApp();

Page({
    data: {
        headerSpacerHeight: 0,
        headerTotalHeight: 0,
        categories: ['证件', '电子产品', '书籍文具', '衣物饰品', '生活用品', '其他'],
        contactTypes: ['手机号', '微信号'],
        contactTypeIndex: 0,

        date: '',
        time: '',
        localImages: [],

        form: {
            type: 0, // 0: Lost, 1: Found
            itemName: '',
            category: '',
            location: '',
            university: '',
            latitude: null,
            longitude: null,
            features: '',
            contactInfo: '',
            contactInfo: '',
            isPublic: 1,
            status: 0
        },

        submitting: false,
        isEditMode: false,
        editId: null
    },

    onLoad(options) {
        this.calcHeaderHeight();
        const now = new Date();
        const y = now.getFullYear();
        const m = (now.getMonth() + 1).toString().padStart(2, '0');
        const d = now.getDate().toString().padStart(2, '0');
        this.setData({
            date: `${y}-${m}-${d}`,
            ['form.university']: wx.getStorageSync('selectedUniversity') || '浙江大学'
        });

        if (options.id && options.mode === 'edit') {
            this.setData({ isEditMode: true, editId: options.id });
            this.loadDetail(options.id);
        }
    },

    onShow() {
        const token = wx.getStorageSync('token');
        if (!token) {
            wx.showToast({ title: '请先登录', icon: 'none' });
            setTimeout(() => { wx.switchTab({ url: '/pages/mine/index' }); }, 1000);
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
            wx.showLoading({ title: '加载中...' });
            const res = await request.get('/lost-found/' + id);
            wx.hideLoading();
            if (res.code === 0 && res.data) {
                const data = res.data;
                // Parse contact
                let cIndex = 0;
                let cInfo = data.contactInfo || '';
                if (cInfo.startsWith('微信号: ')) { cIndex = 1; cInfo = cInfo.substring(5); }
                else if (cInfo.startsWith('手机号: ')) { cIndex = 0; cInfo = cInfo.substring(5); }

                // Parse Images - convert relative paths to full URLs for edit mode
                let imgs = [];
                if (data.imageUrls) {
                    const rawUrls = Array.isArray(data.imageUrls) ? data.imageUrls : data.imageUrls.split(',');
                    imgs = rawUrls.filter(u => u && u.trim()).map(u => app.getFullUrl(u.trim()));
                }

                this.setData({
                    form: {
                        type: data.type != null ? data.type : 0,
                        itemName: data.itemName || '',
                        category: data.category || '',
                        location: data.location || '',
                        university: data.university || '',
                        latitude: data.latitude,
                        longitude: data.longitude,
                        features: data.features || '',
                        contactInfo: cInfo,
                        isPublic: data.isPublic != null ? data.isPublic : 1,
                        status: data.status != null ? data.status : 0
                    },
                    contactTypeIndex: cIndex,
                    localImages: imgs
                });
            }
        } catch (e) {
            wx.hideLoading();
            wx.showToast({ title: '加载失败', icon: 'none' });
        }
    },

    goBack() { wx.navigateBack(); },
    switchType(e) { this.setData({ ['form.type']: parseInt(e.currentTarget.dataset.type) }); },

    // Inputs
    onItemNameInput(e) { this.setData({ ['form.itemName']: e.detail.value }); },
    onLocationInput(e) { this.setData({ ['form.location']: e.detail.value }); },
    onFeaturesInput(e) { this.setData({ ['form.features']: e.detail.value }); },
    onContactInfoInput(e) { this.setData({ ['form.contactInfo']: e.detail.value }); },

    // Pickers
    onCategoryChange(e) { this.setData({ ['form.category']: this.data.categories[e.detail.value] }); },
    onDateChange(e) { this.setData({ date: e.detail.value }); },
    onTimeChange(e) { this.setData({ time: e.detail.value }); },
    onContactTypeChange(e) { this.setData({ contactTypeIndex: e.detail.value }); },
    onPublicChange(e) { this.setData({ ['form.isPublic']: e.detail.value ? 1 : 0 }); },
    onShelfChange(e) { this.setData({ ['form.status']: e.detail.value ? 0 : 1 }); }, // 0=上架, 1=下架

    // Location
    chooseLocation() {
        wx.chooseLocation({
            success: (res) => {
                this.setData({
                    ['form.location']: res.name || res.address,
                    ['form.latitude']: res.latitude,
                    ['form.longitude']: res.longitude
                });
            }
        });
    },

    // Images
    chooseImage() {
        wx.chooseMedia({
            count: 9 - this.data.localImages.length,
            mediaType: ['image'],
            sourceType: ['album', 'camera'],
            success: (res) => {
                const tempFiles = res.tempFiles.map(f => f.tempFilePath);
                this.setData({ localImages: [...this.data.localImages, ...tempFiles] });
            }
        });
    },

    deleteImage(e) {
        const index = e.currentTarget.dataset.index;
        const list = this.data.localImages;
        list.splice(index, 1);
        this.setData({ localImages: list });
    },

    previewImage(e) {
        const index = e.currentTarget.dataset.index;
        wx.previewImage({ urls: this.data.localImages, current: this.data.localImages[index] });
    },

    async submit() {
        if (this.data.submitting) return;
        const { form, localImages, date, time } = this.data;

        if (!form.itemName) return wx.showToast({ title: '请输入物品名称', icon: 'none' });
        if (!form.category) return wx.showToast({ title: '请选择分类', icon: 'none' });
        if (!form.location) return wx.showToast({ title: '请输入地点', icon: 'none' });
        if (!form.features) return wx.showToast({ title: '请输入特征描述', icon: 'none' });
        if (!form.contactInfo) return wx.showToast({ title: '请输入联系方式', icon: 'none' });

        this.setData({ submitting: true });
        wx.showLoading({ title: '发布中...' });

        try {
            // Upload Images
            let uploadedUrls = [];
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

            const prefix = this.data.contactTypes[this.data.contactTypeIndex];
            const fullContact = `${prefix}: ${form.contactInfo}`;
            const fullTime = date ? `${date} ${time || '00:00:00'}` : null;

            const submitData = {
                type: form.type,
                itemName: form.itemName,
                category: form.category,
                time: fullTime,
                location: form.location,
                university: form.university,
                latitude: form.latitude,
                longitude: form.longitude,
                features: form.features,
                imageUrls: uploadedUrls.join(','),
                contactInfo: fullContact,
                isPublic: form.isPublic,
                status: form.status
            };

            if (this.data.isEditMode) {
                submitData.id = parseInt(this.data.editId);
                await request.put('/lost-found/update', submitData);
            } else {
                await request.post('/lost-found/add', submitData);
            }

            wx.hideLoading();
            wx.showToast({ title: '发布成功', icon: 'success' });

            // Emit Refresh
            const pages = getCurrentPages();
            const prevPage = pages[pages.length - 2];
            if (prevPage && prevPage.refreshList) { prevPage.refreshList(); }

            setTimeout(() => { wx.navigateBack(); }, 1000);

        } catch (e) {
            console.error(e);
            wx.hideLoading();
            this.setData({ submitting: false });
            wx.showToast({ title: '发布失败', icon: 'none' });
        }
    }
});
