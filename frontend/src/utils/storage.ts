/**
 * 本地存储工具类
 */
class Storage {
  private prefix: string = 'pindou_timer_'

  /**
   * 获取完整的key
   */
  private getKey(key: string): string {
    return this.prefix + key
  }

  /**
   * 设置localStorage
   */
  set(key: string, value: any): void {
    try {
      const data = JSON.stringify(value)
      localStorage.setItem(this.getKey(key), data)
    } catch (error) {
    }
  }

  /**
   * 获取localStorage
   */
  get<T = any>(key: string): T | null {
    try {
      const data = localStorage.getItem(this.getKey(key))
      return data ? JSON.parse(data) : null
    } catch (error) {
      return null
    }
  }

  /**
   * 删除localStorage
   */
  remove(key: string): void {
    localStorage.removeItem(this.getKey(key))
  }

  /**
   * 清空所有localStorage
   */
  clear(): void {
    localStorage.clear()
  }

  /**
   * 设置sessionStorage
   */
  setSession(key: string, value: any): void {
    try {
      const data = JSON.stringify(value)
      sessionStorage.setItem(this.getKey(key), data)
    } catch (error) {
    }
  }

  /**
   * 获取sessionStorage
   */
  getSession<T = any>(key: string): T | null {
    try {
      const data = sessionStorage.getItem(this.getKey(key))
      return data ? JSON.parse(data) : null
    } catch (error) {
      return null
    }
  }

  /**
   * 删除sessionStorage
   */
  removeSession(key: string): void {
    sessionStorage.removeItem(this.getKey(key))
  }

  /**
   * 清空所有sessionStorage
   */
  clearSession(): void {
    sessionStorage.clear()
  }
}

const storage = new Storage()

export default storage

// 便捷函数
export const setToken = (token: string) => {
  storage.set('token', token)
}

export const getToken = () => {
  return storage.get<string>('token')
}

export const removeToken = () => {
  storage.remove('token')
}
