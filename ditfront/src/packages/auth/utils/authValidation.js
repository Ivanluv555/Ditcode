export const isValidEmail = (email) => {
  const reg = /^[^\s@]+@[^\s@]+\.[^\s@]+$/;
  return reg.test(email);
};

export const isValidUsername = (username) => {
  const reg = /^[a-zA-Z0-9_]{3,20}$/;
  return reg.test(username);
};

export const isValidPassword = (password) => {
  const reg = /^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d!@#$%^&*()_+\-=]{8,32}$/;
  return reg.test(password);
};
