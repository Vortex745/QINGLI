const request = require('../../../utils/request.js');
const util = require('../../../utils/util.js');
const app = getApp();

Page({
    data: {
        headerSpacerHeight: 0,
        headerTotalHeight: 0,
        currentMode: 'quick',

        startDate: '',
        startTime: '',
        minDate: '',
        minTime: '',
        endDate: '',
        endTime: '',

        form: {
            title: '',
            content: '',
            workTime: '',
            location: '',
            workLocation: '',
            salary: '',
            salaryAmount: '',
            settlementMethod: '',
            recruitCount: 1,
            studentRequirement: '',
            contactInfo: '',
            status: 0 // 0-热招中
        },

        salaryUnits: ['元/天', '元/小时', '元/次', '元/月', '元/周'],
        currentSalaryUnit: '元/天',
        settlementDefaults: ['日结', '周结', '月结', '完工结'],

        contactTypes: ['手机号', '微信号'],
        contactTypeIndex: 0,

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
        const hh = now.getHours().toString().padStart(2, '0');
        const mm = now.getMinutes().toString().padStart(2, '0');

        const minDateStr = `${y}-${m}-${d}`;
        const minTimeStr = `${hh}:${mm}`;

        this.setData({
            minDate: minDateStr,
            minTime: minTimeStr,
            startDate: minDateStr,
            startTime: minTimeStr,
            ['form.location']: wx.getStorageSync('selectedUniversity') || '浙江大学'
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
        this.setData({
            headerSpacerHeight: spacerH,
            headerTotalHeight: spacerH + 50
        });
    },

    switchMode(e) {
        const mode = e.currentTarget.dataset.mode;
        this.setData({ currentMode: mode });
    },

    goBack() {
        wx.navigateBack();
    },

    async loadDetail(id) {
        try {
            wx.showLoading({ title: '加载中...' });
            const res = await request.get('/part-time-job/edit/' + id);
            wx.hideLoading();
            if (res.code === 0 && res.data) {
                const data = res.data;

                // Parse Contact
                let cIndex = 0;
                let cInfo = data.contactInfo || '';
                if (cInfo.startsWith('微信号: ')) {
                    cIndex = 1;
                    cInfo = cInfo.substring(5);
                } else if (cInfo.startsWith('手机号: ')) {
                    cIndex = 0;
                    cInfo = cInfo.substring(5);
                }

                // Parse Salary
                let sAmount = '';
                if (data.salary) {
                    const match = data.salary.match(/(\d+)/);
                    if (match) sAmount = match[1];
                }

                this.setData({
                    ['form.title']: data.title || '',
                    ['form.content']: data.content || '',
                    ['form.location']: data.location || '',
                    ['form.workLocation']: data.workLocation || '',
                    ['form.recruitCount']: data.recruitCount || 1,
                    ['form.studentRequirement']: data.studentRequirement || '',
                    ['form.settlementMethod']: data.settlementMethod || '',
                    ['form.salaryAmount']: sAmount,
                    ['form.contactInfo']: cInfo,
                    ['form.status']: data.status != null ? data.status : 0,
                    contactTypeIndex: cIndex,
                    currentMode: (data.content && data.content !== data.title) ? 'full' : this.data.currentMode
                });
            }
        } catch (e) {
            wx.hideLoading();
            wx.showToast({ title: '加载失败', icon: 'none' });
        }
    },

    // Inputs
    onTitleInput(e) { this.setData({ ['form.title']: e.detail.value }); },
    onWorkLocationInput(e) { this.setData({ ['form.workLocation']: e.detail.value }); },
    onSalaryAmountInput(e) { this.setData({ ['form.salaryAmount']: e.detail.value }); },
    onContentInput(e) { this.setData({ ['form.content']: e.detail.value }); },
    onContactInfoInput(e) { this.setData({ ['form.contactInfo']: e.detail.value }); },
    onRecruitCountInput(e) { this.setData({ ['form.recruitCount']: e.detail.value }); },
    onStudentReqInput(e) { this.setData({ ['form.studentRequirement']: e.detail.value }); },

    // Pickers
    onSalaryUnitChange(e) { this.setData({ currentSalaryUnit: this.data.salaryUnits[e.detail.value] }); },
    onContactTypeChange(e) { this.setData({ contactTypeIndex: e.detail.value }); },
    onSettlementChange(e) { this.setData({ ['form.settlementMethod']: this.data.settlementDefaults[e.detail.value] }); },
    onShelfStatusChange(e) { this.setData({ ['form.status']: e.detail.value ? 0 : 1 }); }, // 0=上架/热招, 1=下架

    // Date Time
    onStartDateChange(e) { this.setData({ startDate: e.detail.value }); this.updateWorkTime(); },
    onStartTimeChange(e) { this.setData({ startTime: e.detail.value }); this.updateWorkTime(); },
    onEndDateChange(e) { this.setData({ endDate: e.detail.value }); this.updateWorkTime(); },
    onEndTimeChange(e) { this.setData({ endTime: e.detail.value }); this.updateWorkTime(); },

    updateWorkTime() {
        const { startDate, startTime, endDate, endTime } = this.data;
        const start = startDate ? `${startDate} ${startTime || '00:00'}` : '';
        const end = endDate ? `${endDate} ${endTime || '00:00'}` : '';
        let wt = '';
        if (start && end) wt = `${start} 至 ${end}`;
        else if (start) wt = start;
        this.setData({ ['form.workTime']: wt });
    },

    async submit() {
        if (this.data.submitting) return;
        const form = this.data.form;

        if (!form.title) return wx.showToast({ title: '请输入岗位名称', icon: 'none' });
        if (!this.data.startDate) return wx.showToast({ title: '请选择开始时间', icon: 'none' });

        if (!form.workLocation) return wx.showToast({ title: '请输入工作地点', icon: 'none' });
        if (!form.salaryAmount) return wx.showToast({ title: '请输入薪资', icon: 'none' });

        if (this.data.currentMode === 'full') {
            if (!form.content) return wx.showToast({ title: '请输入详细要求', icon: 'none' });
        } else {
            if (!form.content) return wx.showToast({ title: '请输入工作详情', icon: 'none' });
        }

        if (!form.contactInfo) return wx.showToast({ title: '请输入联系方式', icon: 'none' });

        this.setData({ submitting: true });
        wx.showLoading({ title: '发布中...' });

        try {
            const prefix = this.data.contactTypes[this.data.contactTypeIndex];
            const fullContact = `${prefix}: ${form.contactInfo}`;
            const fullSalary = `${form.salaryAmount}${this.data.currentSalaryUnit}`;

            const submitData = {
                title: form.title,
                content: form.content,
                workTime: form.workTime,
                location: form.location,
                workLocation: form.workLocation,
                salary: fullSalary,
                contactInfo: fullContact,
                recruitCount: parseInt(form.recruitCount) || 1,
                settlementMethod: form.settlementMethod || '完工结',
                studentRequirement: form.studentRequirement || '无限制',
                status: form.status
            };

            // Validation for dates
            if (this.data.endDate) {
                const s = new Date(`${this.data.startDate} ${this.data.startTime || '00:00'}`.replace(/-/g, '/'));
                const e = new Date(`${this.data.endDate} ${this.data.endTime || '23:59'}`.replace(/-/g, '/'));
                if (e <= s) {
                    wx.hideLoading();
                    this.setData({ submitting: false });
                    return wx.showToast({ title: '结束时间需晚于开始时间', icon: 'none' });
                }
            }

            if (this.data.isEditMode) {
                submitData.id = parseInt(this.data.editId);
                await request.put('/part-time-job/update', submitData);
            } else {
                await request.post('/part-time-job/add', submitData);
            }

            wx.hideLoading();
            wx.showToast({ title: 'Success', icon: 'success' });

            this.setData({
                form: {
                    title: '',
                    content: '',
                    workTime: '',
                    location: form.location, // keep loc
                    workLocation: '',
                    salary: '',
                    salaryAmount: '',
                    settlementMethod: '',
                    recruitCount: 1,
                    studentRequirement: '',
                    contactInfo: ''
                },
                startDate: '', startTime: '', endDate: '', endTime: ''
            });

            setTimeout(() => { wx.navigateBack(); }, 1000);

        } catch (err) {
            wx.hideLoading();
            this.setData({ submitting: false });
            wx.showToast({ title: '提交失败', icon: 'none' });
        }
    }
});
