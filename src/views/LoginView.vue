<template>
  <div class="login-wrapper">
    <div class="login-card">
      <div class="brand">
        <div class="brand-logo">FS</div>
        <h1 class="brand-name">FlowSync</h1>
        <p class="brand-slogan">小组任务协同管理系统</p>
      </div>

      <el-form :model="loginForm" :rules="rules" ref="loginFormRef" label-position="top">
        <el-form-item label="账号" prop="username">
          <el-input 
            v-model="loginForm.username" 
            placeholder="请输入账号" 
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="loginForm.password" 
            type="password" 
            placeholder="请输入密码" 
            prefix-icon="Lock" 
            show-password
            @keyup.enter="handleLogin"
          />
        </el-form-item>

        <div class="form-actions">
          <el-button type="primary" :loading="loading" class="btn-login" @click="handleLogin">
            登录
          </el-button>
          <el-button class="btn-register" @click="goToRegister">
            注册账号
          </el-button>
        </div>
      </el-form>

      <div class="demo-accounts">
        <p class="demo-title">内置测试账号 (密码 123456)：</p>
        <div class="demo-tags">
          <el-tag size="small" type="info" @click="fillAccount('leader')">负责人: leader</el-tag>
          <el-tag size="small" type="info" @click="fillAccount('member1')">成员: member1</el-tag>
          <el-tag size="small" type="info" @click="fillAccount('member2')">成员: member2</el-tag>
        </div>
      </div>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api'
import { ElMessage } from 'element-plus'

export default {
  name: 'LoginView',
  setup() {
    const router = useRouter()
    const loginFormRef = ref(null)
    const loading = ref(false)

    const loginForm = ref({
      username: '',
      password: ''
    })

    const rules = {
      username: [
        { required: true, message: '请输入用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度需在3到20个字符之间', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请输入密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度需在6到20个字符之间', trigger: 'blur' }
      ]
    }

    const handleLogin = () => {
      loginFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        loading.value = true
        try {
          const res = await authApi.login(loginForm.value.username, loginForm.value.password)
          if (res.success) {
            ElMessage.success({
              message: res.message || '登录成功',
              duration: 1500
            })
            // 将用户信息（剔除密码）和简易 token 存入 sessionStorage
            const userData = res.data || {}
            sessionStorage.setItem('user', JSON.stringify(userData))
            // 跳转至首页
            router.push('/')
          } else {
            ElMessage.error(res.message || '登录失败，账号或密码错误')
          }
        } catch (error) {
          console.error(error)
        } finally {
          loading.value = false
        }
      })
    }

    const goToRegister = () => {
      router.push('/register')
    }

    const fillAccount = (username) => {
      loginForm.value.username = username
      loginForm.value.password = '123456'
    }

    return {
      loginForm,
      rules,
      loginFormRef,
      loading,
      handleLogin,
      goToRegister,
      fillAccount
    }
  }
}
</script>

<style scoped>
.login-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-page);
  padding: 20px;
}

.login-card {
  width: 100%;
  max-width: 420px;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 40px 32px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.brand {
  text-align: center;
  margin-bottom: 30px;
}

.brand-logo {
  width: 48px;
  height: 48px;
  background-color: var(--google-blue);
  color: white;
  font-family: 'Outfit', sans-serif;
  font-weight: 700;
  font-size: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  margin: 0 auto 12px;
  box-shadow: 0 2px 6px rgba(26, 115, 232, 0.3);
}

.brand-name {
  font-family: 'Outfit', sans-serif;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

.brand-slogan {
  color: var(--text-secondary);
  font-size: 13px;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  flex-direction: column;
  gap: 12px;
  margin-top: 24px;
}

.btn-login {
  height: 42px;
  font-size: 15px;
}

.btn-register {
  height: 42px;
  font-size: 15px;
}

.demo-accounts {
  margin-top: 30px;
  border-top: 1px solid var(--border-color);
  padding-top: 20px;
}

.demo-title {
  color: var(--text-secondary);
  font-size: 12px;
  margin-bottom: 10px;
  text-align: center;
}

.demo-tags {
  display: flex;
  justify-content: center;
  gap: 8px;
}

.demo-tags .el-tag {
  cursor: pointer;
  transition: var(--transition-bezier);
}

.demo-tags .el-tag:hover {
  background-color: var(--google-blue-light);
  color: var(--google-blue);
  border-color: var(--google-blue);
}
</style>
