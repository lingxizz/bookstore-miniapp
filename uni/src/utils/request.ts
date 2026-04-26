// API请求基础地址，空=同域（通过proxy转发到Java后端）
const BASE_URL = ''

function getToken() {
  return uni.getStorageSync('token') || ''
}

export function request<T = any>(url: string, method: string = 'GET', data?: any): Promise<T> {
  return new Promise((resolve, reject) => {
    uni.request({
      url: `${BASE_URL}${url}`,
      method: method as any,
      data,
      header: {
        Authorization: `Bearer ${getToken()}`,
      },
      success: (res: any) => {
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
      fail: (err: any) => reject(err),
    })
  })
}

export function get<T = any>(url: string, data?: any) {
  return request<T>(url, 'GET', data)
}

export function post<T = any>(url: string, data?: any) {
  return request<T>(url, 'POST', data ?? {})
}

export function del<T = any>(url: string, data?: any) {
  return request<T>(url, 'DELETE', data)
}