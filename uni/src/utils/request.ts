const BASE_URL = ''

function getToken() {
  return uni.getStorageSync('token') || ''
}

export function request<T = any>(options: UniApp.RequestOptions): Promise<T> {
  return new Promise((resolve, reject) => {
    uni.request({
      ...options,
      url: `${BASE_URL}${options.url}`,
      header: {
        ...(options.header || {}),
        Authorization: `Bearer ${getToken()}`,
      },
      success: (res) => {
        if (res.statusCode >= 200 && res.statusCode < 300) {
          resolve(res.data as T)
        } else if (res.statusCode === 401 || res.statusCode === 403) {
          uni.removeStorageSync('token')
          uni.navigateTo({ url: '/pages/me/me' })
          reject(new Error('Unauthorized'))
        } else {
          reject(new Error(`HTTP ${res.statusCode}`))
        }
      },
      fail: (err) => reject(err),
    })
  })
}

export function get<T = any>(url: string, data?: any) {
  return request<T>({ url, method: 'GET', data })
}

export function post<T = any>(url: string, data?: any) {
  return request<T>({ url, method: 'POST', data: data ?? {} })
}

export function del<T = any>(url: string, data?: any) {
  return request<T>({ url, method: 'DELETE', data })
}
