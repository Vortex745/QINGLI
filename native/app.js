/** app.js **/
App({
    globalData: {
        userInfo: null,
        apiBaseUrl: 'http://192.168.5.17:8081/api', // Updated to current machine IP
        squareTab: null
    },
    onLaunch() {
        // Shared Logic
        const token = wx.getStorageSync('token');
        if (token) {
            // Validation logic
        }
    },
    getFullUrl(path, type = 'avatar') {
        if (!path || path === 'undefined' || path === 'null') {
            return type === 'avatar' ? '/assets/images/avatar_default.png' : '/assets/images/default-image.svg';
        }

        // Handle comma separated multiple images
        let firstPath = String(path).split(',')[0].trim();
        if (!firstPath) return type === 'avatar' ? '/assets/images/avatar_default.png' : '/assets/images/default-image.svg';

        // 0. Determine Target Origin based on platform
        const sysInfo = wx.getSystemInfoSync();
        const configBase = this.globalData.apiBaseUrl; // e.g., http://192.168.5.10:8081/api
        let targetOrigin = '';

        if (sysInfo.platform === 'devtools' || sysInfo.platform === 'windows' || sysInfo.platform === 'mac') {
            // PC/DevTools: Force 127.0.0.1 to handle Mixed Content and machine-local requests
            targetOrigin = 'http://127.0.0.1:8081';
        } else {
            // Mobile (Android/iOS): Use LAN IP from apiBaseUrl
            targetOrigin = configBase.replace(/\/api$/, '');
        }

        // 1. Check if it's a full URL
        if (firstPath.startsWith('http')) {
            // Fix third-party images
            if (firstPath.includes('wx.qlogo.cn') || firstPath.includes('mmbiz.qpic.cn')) {
                return firstPath.replace('http://', 'https://');
            }

            // Rewrite local patterns to targetOrigin
            let newPath = firstPath
                .replace(/http:\/\/localhost:\d+/g, targetOrigin)
                .replace(/http:\/\/127\.0\.0\.1:\d+/g, targetOrigin)
                .replace(/http:\/\/192\.168\.\d+\.\d+:\d+/g, targetOrigin);

            // Strip duplicate /api/api
            return newPath.replace(/\/api\/api\//g, '/api/');
        }

        // 2. Local assets
        if (firstPath.startsWith('/assets/') || firstPath.startsWith('assets/')) {
            return firstPath.startsWith('/') ? firstPath : '/' + firstPath;
        }

        // 3. Construct from relative path
        let cleanPath = firstPath.startsWith('/') ? firstPath.substring(1) : firstPath;

        // Ensure we don't double up 'api/' prefix
        if (cleanPath.startsWith('api/')) {
            cleanPath = cleanPath.substring(4);
        }

        // Most static images move through /profile/ in backend mappings
        // Since backend context-path is /api, the full URL must be /api/profile/...
        if (cleanPath.startsWith('profile/')) {
            return targetOrigin + '/api/' + cleanPath;
        }

        return targetOrigin + '/api/' + cleanPath;
    }
})
