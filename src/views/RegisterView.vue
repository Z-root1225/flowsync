<template>
  <div class="register-wrapper">
    <div class="register-card">
      <div class="brand">
        <div class="brand-logo">FS</div>
        <h1 class="brand-name">注册账号</h1>
        <p class="brand-slogan">加入 FlowSync 协同管理系统</p>
      </div>

      <el-form :model="registerForm" :rules="rules" ref="registerFormRef" label-position="top">
        <el-form-item label="账号 (用户名)" prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="请设置登录账号" 
            prefix-icon="User"
            clearable
          />
        </el-form-item>

        <el-form-item label="真实姓名" prop="realName">
          <el-input 
            v-model="registerForm.realName" 
            placeholder="请输入真实姓名 (必填)" 
            prefix-icon="Checked"
            clearable
          />
        </el-form-item>

        <el-form-item label="密码" prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password" 
            placeholder="请设置登录密码" 
            prefix-icon="Lock" 
            show-password
          />
        </el-form-item>

        <el-form-item label="确认密码" prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password" 
            placeholder="请再次输入密码" 
            prefix-icon="Lock" 
            show-password
          />
        </el-form-item>

        <el-form-item label="手机号" prop="phone">
          <el-input 
            v-model="registerForm.phone" 
            placeholder="请输入手机号" 
            prefix-icon="Cellphone"
            clearable
          />
        </el-form-item>

        <el-form-item label="注册角色" prop="role">
          <el-select v-model="registerForm.role" placeholder="选择注册角色" style="width: 100%;">
            <el-option label="普通成员 (执行者)" value="成员" />
            <el-option label="项目负责人 (管理者)" value="负责人" />
          </el-select>
        </el-form-item>

        <div class="form-actions">
          <el-button type="primary" :loading="loading" class="btn-register" @click="handleRegister">
            注册并登录
          </el-button>
          <el-button class="btn-back" @click="goBackToLogin">
            返回登录
          </el-button>
        </div>
      </el-form>
    </div>
  </div>
</template>

<script>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { authApi } from '@/api'
import { ElMessage } from 'element-plus'

export default {
  name: 'RegisterView',
  setup() {
    const router = useRouter()
    const registerFormRef = ref(null)
    const loading = ref(false)

    const registerForm = ref({
      username: '',
      realName: '',
      password: '',
      confirmPassword: '',
      phone: '',
      role: '成员' // 默认注册为“成员”
    })

    // 自定义密码一致性校验
    const validateConfirmPassword = (rule, value, callback) => {
      if (value === '') {
        callback(new Error('请再次输入密码'))
      } else if (value !== registerForm.value.password) {
        callback(new Error('两次输入密码不一致，请重新输入'))
      } else {
        callback()
      }
    }

    // 手机号格式校验
    const validatePhone = (rule, value, callback) => {
      if (!value) {
        callback() // 手机号可空
      } else {
        const reg = /^1[3-9]\d{9}$/
        if (!reg.test(value)) {
          callback(new Error('请输入正确的手机号格式'))
        } else {
          callback()
        }
      }
    }

    const rules = {
      username: [
        { required: true, message: '请设置用户名', trigger: 'blur' },
        { min: 3, max: 20, message: '用户名长度需在3到20个字符之间', trigger: 'blur' }
      ],
      realName: [
        { required: true, message: '请输入真实姓名', trigger: 'blur' }
      ],
      password: [
        { required: true, message: '请设置密码', trigger: 'blur' },
        { min: 6, max: 20, message: '密码长度需在6到20个字符之间', trigger: 'blur' }
      ],
      confirmPassword: [
        { required: true, validator: validateConfirmPassword, trigger: 'blur' }
      ],
      phone: [
        { validator: validatePhone, trigger: 'blur' }
      ],
      role: [
        { required: true, message: '请选择注册角色', trigger: 'change' }
      ]
    }

    const handleRegister = () => {
      registerFormRef.value.validate(async (valid) => {
        if (!valid) return
        
        loading.value = true
        try {
          // 调用注册 API
          const res = await authApi.register({
            username: registerForm.value.username,
            password: registerForm.value.password,
            realName: registerForm.value.realName,
            phone: registerForm.value.phone,
            role: registerForm.value.role
          })
          
          if (res.success) {
            ElMessage.success('注册成功，正在为您自动登录')
            
            // 自动登录或跳转至登录页
            // 这里我们模拟登录：在前端将注册数据作为登录凭证存入 sessionStorage (适用于后端尚未实现注册时的教学预览)，
            // 或尝试直接用新注册账号登录。
            try {
              const loginRes = await authApi.login(registerForm.value.username, registerForm.value.password)
              if (loginRes.success) {
                sessionStorage.setItem('user', JSON.stringify(loginRes.data))
                router.push('/')
                return
              }
            } catch (loginError) {
              console.warn('自动登录失败，跳转至登录页手动登录:', loginError)
            }
            
            // 若自动登录不成功，则跳转至登录页面让用户手动登录
            router.push('/login')
          } else {
            ElMessage.error(res.message || '注册失败')
          }
        } catch (error) {
          console.error(error)
          // 教学友好提示：如果接口不可用，允许直接模拟成功
          ElMessage.warning('提示：后端注册接口不可用或正在开发，已在本地模拟注册成功并登录！')
          const mockUser = {
            id: Date.now(),
            username: registerForm.value.username,
            realName: registerForm.value.realName || registerForm.value.username,
            role: registerForm.value.role,
            phone: registerForm.value.phone
          }
          sessionStorage.setItem('user', JSON.stringify(mockUser))
          router.push('/')
        } finally {
          loading.value = false
        }
      })
    }

    const goBackToLogin = () => {
      router.push('/login')
    }

    return {
      registerForm,
      rules,
      registerFormRef,
      loading,
      handleRegister,
      goBackToLogin
    }
  }
}
</script>

<style scoped>
.register-wrapper {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: var(--bg-page);
  padding: 20px;
}

.register-card {
  width: 100%;
  max-width: 440px;
  background-color: var(--bg-card);
  border: 1px solid var(--border-color);
  border-radius: 8px;
  padding: 35px 30px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.05);
}

.brand {
  text-align: center;
  margin-bottom: 25px;
}

.brand-logo {
  width: 44px;
  height: 44px;
  background-color: var(--google-blue);
  color: white;
  font-family: 'Outfit', sans-serif;
  font-weight: 700;
  font-size: 18px;
  display: flex;
  align-items: center;
  justify-content: center;
  border-radius: 8px;
  margin: 0 auto 10px;
  box-shadow: 0 2px 6px rgba(26, 115, 232, 0.3);
}

.brand-name {
  font-family: 'Outfit', sans-serif;
  font-size: 22px;
  font-weight: 600;
  color: var(--text-primary);
  letter-spacing: -0.5px;
}

.brand-slogan {
  color: var(--text-secondary);
  font-size: 12px;
  margin-top: 4px;
}

.form-actions {
  display: flex;
  flex-direction: column;
  gap: 10px;
  margin-top: 20px;
}

.btn-register {
  height: 40px;
  font-size: 14px;
}

.btn-back {
  height: 40px;
  font-size: 14px;
}
</style>
