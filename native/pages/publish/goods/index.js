const request = require('../../../utils/request.js');
const app = getApp();

Page({
    data: {
        headerSpacerHeight: 0,
        headerTotalHeight: 0,
        categories: ['电子产品', '书籍教材', '衣物鞋包', '生活用品', '美妆护肤', '运动户外', '食品饮料', '其他'],
        conditions: ['全新', '几乎全新', '轻微使用痕迹', '明显使用痕迹'],

        formData: {
            name: '',
            price: '',
            originalPrice: '',
            description: '',
            category: '',
            condition: '',
            location: '',
            bargainAllowed: 1,
            imageUrl: '',
            status: 0
        },
        imageList: [],
        submitting: false,
        isEditMode: false,
        editId: null
    },

    onLoad(options) {
        this.calcHeaderHeight();

        // Set default location from storage
        const currentUni = wx.getStorageSync('selectedUniversity') || '';
        this.setData({
            ['formData.location']: currentUni
        });

        if (options.id && options.mode === 'edit') {
            this.setData({
                isEditMode: true,
                editId: options.id
            });
            this.loadGoodsDetail(options.id);
        }
    },

    onShow() {
        const token = wx.getStorageSync('token');
        if (!token) {
            wx.showToast({ title: '请先登录', icon: 'none' });
            setTimeout(() => {
                wx.switchTab({ url: '/pages/mine/index' });
            }, 1000);
        }
    },

    calcHeaderHeight() {
        const sysInfo = wx.getSystemInfoSync();
        let top = sysInfo.statusBarHeight;
        let spacerH = top + 44;
        const menuButton = wx.getMenuButtonBoundingClientRect();
        if (menuButton) {
            spacerH = menuButton.bottom + 8;
        }
        this.setData({
            headerSpacerHeight: spacerH,
            headerTotalHeight: spacerH + 50
        });
    },

    async loadGoodsDetail(id) {
        try {
            wx.showLoading({ title: '加载中...' });
            const res = await request.get('/goods/' + id);
            wx.hideLoading();
            if (res.code === 0 && res.data) {
                const data = res.data;
                // Process images - convert relative paths to full URLs for edit mode
                let imageList = [];
                if (data.imageUrl) {
                    const urls = data.imageUrl.split(',').filter(u => u.trim());
                    imageList = urls.map(u => app.getFullUrl(u.trim()));
                }
                this.setData({
                    ['formData.name']: data.name || '',
                    ['formData.price']: data.price ? data.price.toString() : '',
                    ['formData.originalPrice']: data.originalPrice ? data.originalPrice.toString() : '',
                    ['formData.description']: data.description || '',
                    ['formData.category']: data.category || '',
                    ['formData.condition']: data.condition || '',
                    ['formData.location']: data.location || '',
                    ['formData.bargainAllowed']: data.bargainAllowed != null ? data.bargainAllowed : 1,
                    ['formData.status']: data.status != null ? data.status : 0,
                    imageList: imageList
                });
            }
        } catch (e) {
            wx.hideLoading();
            wx.showToast({ title: '加载失败', icon: 'none' });
        }
    },

    goBack() {
        if (this.data.formData.name || this.data.formData.description || this.data.imageList.length > 0) {
            wx.showModal({
                title: '提示',
                content: '确定要放弃编辑吗？',
                success: (res) => {
                    if (res.confirm) {
                        wx.navigateBack();
                    }
                }
            });
        } else {
            wx.navigateBack();
        }
    },

    // Inputs
    onNameInput(e) { this.setData({ ['formData.name']: e.detail.value }); },
    onPriceInput(e) { this.setData({ ['formData.price']: e.detail.value }); },
    onOriginalPriceInput(e) { this.setData({ ['formData.originalPrice']: e.detail.value }); },
    onDescriptionInput(e) { this.setData({ ['formData.description']: e.detail.value }); },

    // Pickers
    // Pickers
    onCategoryChange(e) { this.setData({ ['formData.category']: this.data.categories[e.detail.value] }); },
    onConditionChange(e) { this.setData({ ['formData.condition']: this.data.conditions[e.detail.value] }); },


    // Switches
    onBargainChange(e) { this.setData({ ['formData.bargainAllowed']: e.detail.value ? 1 : 0 }); },
    onStatusChange(e) { this.setData({ ['formData.status']: e.detail.value ? 0 : 3 }); }, // 0=shelf, 3=off-shelf

    // Image
    chooseImage() {
        wx.chooseMedia({
            count: 9 - this.data.imageList.length,
            mediaType: ['image'],
            sourceType: ['album', 'camera'],
            success: (res) => {
                const tempFiles = res.tempFiles.map(f => f.tempFilePath);
                this.setData({
                    imageList: [...this.data.imageList, ...tempFiles]
                });
            }
        });
    },

    deleteImage(e) {
        const index = e.currentTarget.dataset.index;
        const list = this.data.imageList;
        list.splice(index, 1);
        this.setData({ imageList: list });
    },

    previewImage(e) {
        const index = e.currentTarget.dataset.index;
        wx.previewImage({
            urls: this.data.imageList,
            current: this.data.imageList[index]
        });
    },

    async submit() {
        if (this.data.submitting) return;

        const { formData, imageList } = this.data;

        // Validation
        if (!formData.name) return wx.showToast({ title: '请输入物品名称', icon: 'none' });
        if (!formData.price) return wx.showToast({ title: '请输入价格', icon: 'none' });
        if (imageList.length === 0) return wx.showToast({ title: '请上传至少一张图片', icon: 'none' });

        this.setData({ submitting: true });
        wx.showLoading({ title: this.data.isEditMode ? '保存中...' : '发布中...' });

        try {
            // 1. Upload images
            let uploadedUrls = [];
            if (imageList.length > 0) {
                for (let imgPath of imageList) {
                    const isRemote = (imgPath.startsWith('http') || imgPath.startsWith('https')) && !imgPath.includes('http://tmp') && !imgPath.includes('wxfile');

                    if (isRemote) {
                        // Extract relative path from full URL (e.g., http://xxx/api/profile/xxx.jpg -> /profile/xxx.jpg)
                        let relativePath = imgPath;
                        const profileIndex = imgPath.indexOf('/profile/');
                        if (profileIndex !== -1) {
                            relativePath = imgPath.substring(profileIndex);
                        }
                        uploadedUrls.push(relativePath);
                    } else {
                        try {
                            const url = await request.uploadFile(imgPath);
                            if (url) uploadedUrls.push(url);
                        } catch (e) {
                            console.error('Image upload failed', e);
                            wx.showToast({ title: '部分图片上传失败', icon: 'none' });
                        }
                    }
                }
            }

            if (uploadedUrls.length === 0 && imageList.length > 0) {
                throw new Error('所有图片上传失败');
            }

            // 2. Prepare Data
            const submitData = {
                name: formData.name.trim(),
                description: formData.description.trim(),
                price: parseFloat(formData.price),
                imageUrl: uploadedUrls.join(','),
                originalPrice: formData.originalPrice ? parseFloat(formData.originalPrice) : null,
                category: formData.category || null,
                condition: formData.condition || null,
                location: formData.location || null,
                bargainAllowed: formData.bargainAllowed
            };

            if (this.data.isEditMode) {
                submitData.id = parseInt(this.data.editId);
                submitData.status = formData.status;
                await request.put('/goods/update', submitData);
            } else {
                await request.post('/goods/add', submitData);
            }

            wx.hideLoading();
            wx.showToast({ title: this.data.isEditMode ? '保存成功' : '发布成功', icon: 'success' });

            // TODO: Emit global event if needed, or just standard navigation
            // We don't have uni.$emit, but we can set flags in globalData or use page stack
            const pages = getCurrentPages();
            const prevPage = pages[pages.length - 2];
            if (prevPage && prevPage.refreshList) {
                prevPage.refreshList();
            }

            this.setData({
                formData: { name: '', price: '', description: '', imageUrl: '', status: 0, bargainAllowed: 1 },
                imageList: []
            });

            setTimeout(() => {
                wx.navigateBack();
            }, 1000);

        } catch (err) {
            console.error(err);
            wx.hideLoading();
            this.setData({ submitting: false });
            wx.showToast({ title: '操作失败', icon: 'none' });
        }
    }
});
