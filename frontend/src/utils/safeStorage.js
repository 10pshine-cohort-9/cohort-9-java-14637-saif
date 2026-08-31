const memoryStorage = {};

export const safeStorage = {
  getItem(key) {
    if (Object.prototype.hasOwnProperty.call(memoryStorage, key)) {
      return memoryStorage[key];
    }
    try {
      return localStorage.getItem(key);
    } catch (e) {
      console.warn(`localStorage.getItem failed for key "${key}":`, e);
      return null;
    }
  },
  setItem(key, value) {
    try {
      localStorage.setItem(key, value);
      delete memoryStorage[key];
    } catch (e) {
      console.warn(`localStorage.setItem failed for key "${key}":`, e);
      memoryStorage[key] = value;
    }
  },
  removeItem(key) {
    try {
      localStorage.removeItem(key);
    } catch (e) {
      console.warn(`localStorage.removeItem failed for key "${key}":`, e);
    }
    delete memoryStorage[key];
  }
};
