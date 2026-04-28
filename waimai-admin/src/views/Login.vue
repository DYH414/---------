<template>
  <a-config-provider :theme="antdTheme">
    <div class="login-page" :data-theme="isDark ? 'dark' : 'light'">
      <div class="login-bg" aria-hidden="true" />
      <div class="login-frame">
        <div class="login-panel">
          <header class="login-panel__toolbar">
            <button type="button" class="login-theme-btn" :aria-pressed="isDark" @click="toggleTheme">
              <span class="login-theme-thumb" aria-hidden="true" />
              {{ isDark ? '浅色' : '深色' }}
            </button>
          </header>

          <div class="login-panel__body">
            <section class="login-panel__intro" aria-labelledby="login-intro-title">
              <div class="login-brand">
                <div class="login-mark">WM</div>
                <div>
                  <div class="login-brand-title">校园外卖聚合</div>
                  <div class="login-brand-sub">管理后台</div>
                </div>
              </div>
              <h1 id="login-intro-title" class="login-headline">统一运营商家、分类与推荐配置</h1>
              <p class="login-lead">
                与平台数据面板、审核流和风控策略对齐。登录后可在「商家」「分类」「每日推荐」等模块完成日常运维。
              </p>
              <ul class="login-meta" aria-label="环境说明">
                <li>
                  <span class="login-dot login-dot--ok" />
                  API 基址：<code class="login-code">{{ apiBase }}</code>
                </li>
                <li>
                  <span class="login-dot login-dot--muted" />
                  鉴权方式：JWT（Bearer），与后端 <code class="login-code">/api/admin/auth/login</code> 对齐
                </li>
              </ul>
            </section>

            <div class="login-panel__divider" aria-hidden="true" />

            <section class="login-panel__form" aria-labelledby="login-form-title">
              <header class="login-card-head">
                <h2 id="login-form-title" class="login-card-title">管理员登录</h2>
                <p class="login-card-desc">请输入后台账号与密码</p>
              </header>

              <a-form
                class="login-form"
                :model="form"
                layout="vertical"
                required-mark-position="end"
                @finish="handleLogin"
              >
                <a-form-item
                  label="用户名"
                  name="username"
                  :rules="[{ required: true, message: '请输入用户名' }]"
                >
                  <a-input
                    v-model:value="form.username"
                    size="large"
                    placeholder="例如 admin"
                    autocomplete="username"
                    :disabled="loading"
                  >
                    <template #prefix>
                      <UserOutlined class="login-input-icon" />
                    </template>
                  </a-input>
                </a-form-item>

                <a-form-item
                  label="密码"
                  name="password"
                  :rules="[{ required: true, message: '请输入密码' }]"
                >
                  <a-input-password
                    v-model:value="form.password"
                    size="large"
                    placeholder="请输入密码"
                    autocomplete="current-password"
                    :disabled="loading"
                  >
                    <template #prefix>
                      <LockOutlined class="login-input-icon" />
                    </template>
                  </a-input-password>
                </a-form-item>

                <div class="login-row">
                  <a-checkbox v-model:checked="rememberUsername" :disabled="loading">
                    记住用户名
                  </a-checkbox>
                </div>

                <a-form-item class="login-submit-wrap">
                  <a-button type="primary" html-type="submit" size="large" block :loading="loading">
                    登录
                  </a-button>
                </a-form-item>
              </a-form>
            </section>
          </div>
        </div>

        <p class="login-footnote">校园外卖与二手平台 · 管理端</p>
      </div>
    </div>
  </a-config-provider>
</template>

<script setup lang="ts">
import { computed, onMounted, reactive, ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { message, theme as antdThemeToken } from 'ant-design-vue'
import { UserOutlined, LockOutlined } from '@ant-design/icons-vue'
import { adminLogin } from '@/api/auth'
import { useUserStore } from '@/store/user'

const router = useRouter()
const userStore = useUserStore()

const form = reactive({
  username: '',
  password: ''
})

const loading = ref(false)
const rememberUsername = ref(true)
const isDark = ref(false)

const apiBase = import.meta.env.VITE_API_BASE_URL || '/api'

const THEME_KEY = 'waimai-admin-login-theme'
const USERNAME_KEY = 'waimai-admin-remembered-username'

const antdTheme = computed(() => ({
  algorithm: isDark.value ? antdThemeToken.darkAlgorithm : antdThemeToken.defaultAlgorithm,
  token: {
    colorPrimary: '#4f46e5',
    borderRadius: 10,
    fontFamily:
      "ui-sans-serif, system-ui, -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'PingFang SC', 'Microsoft YaHei', sans-serif"
  }
}))

function readStoredTheme() {
  try {
    const v = localStorage.getItem(THEME_KEY)
    if (v === 'dark' || v === 'light') return v === 'dark'
    return window.matchMedia('(prefers-color-scheme: dark)').matches
  } catch {
    return false
  }
}

function persistTheme() {
  try {
    localStorage.setItem(THEME_KEY, isDark.value ? 'dark' : 'light')
  } catch {
    /* ignore */
  }
}

function toggleTheme() {
  isDark.value = !isDark.value
}

watch(isDark, persistTheme)

onMounted(() => {
  isDark.value = readStoredTheme()
  try {
    const u = localStorage.getItem(USERNAME_KEY)
    if (u) form.username = u
  } catch {
    /* ignore */
  }
})

async function handleLogin() {
  loading.value = true
  try {
    const res = await adminLogin(form.username, form.password)
    userStore.setToken(res.token)
    userStore.setUsername(form.username)
    try {
      if (rememberUsername.value) {
        localStorage.setItem(USERNAME_KEY, form.username)
      } else {
        localStorage.removeItem(USERNAME_KEY)
      }
    } catch {
      /* ignore */
    }
    message.success('登录成功')
    await router.push('/')
  } catch (e: unknown) {
    const msg = e instanceof Error ? e.message : '登录失败'
    message.error(msg)
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.login-page {
  --login-radius: 16px;
  --login-shadow:
    0 1px 0 rgb(15 23 42 / 0.04),
    0 24px 48px -20px rgb(15 23 42 / 0.12),
    0 0 0 1px rgb(15 23 42 / 0.04);
  --login-border: rgb(226 232 240 / 0.85);
  --login-panel-bg: rgb(255 255 255 / 0.78);
  --login-form-well: rgb(248 250 252 / 0.65);
  --login-text: rgb(15 23 42);
  --login-muted: rgb(100 116 139);
  --login-accent-soft: rgb(99 102 241 / 0.12);

  position: relative;
  min-height: 100vh;
  overflow-x: hidden;
  overflow-y: auto;
  color: var(--login-text);
  background: rgb(241 245 249);
}

.login-page[data-theme='dark'] {
  --login-shadow:
    0 1px 0 rgb(255 255 255 / 0.06) inset,
    0 24px 48px -20px rgb(0 0 0 / 0.55),
    0 0 0 1px rgb(255 255 255 / 0.06);
  --login-border: rgb(63 63 70 / 0.5);
  --login-panel-bg: rgb(24 24 27 / 0.72);
  --login-form-well: rgb(9 9 11 / 0.35);
  --login-text: rgb(250 250 250);
  --login-muted: rgb(161 161 170);
  --login-accent-soft: rgb(129 140 248 / 0.15);
  background: rgb(9 9 11);
}

.login-bg {
  pointer-events: none;
  position: absolute;
  inset: 0;
  background:
    radial-gradient(ellipse 80% 50% at 50% -20%, rgb(99 102 241 / 0.09), transparent),
    radial-gradient(circle at 80% 20%, rgb(148 163 184 / 0.2), transparent 45%);
}

.login-page[data-theme='dark'] .login-bg {
  background:
    radial-gradient(ellipse 80% 50% at 50% -20%, rgb(99 102 241 / 0.14), transparent),
    radial-gradient(circle at 80% 20%, rgb(82 82 91 / 0.35), transparent 50%);
}

.login-frame {
  position: relative;
  z-index: 1;
  display: flex;
  min-height: 100vh;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 1.5rem 1rem 2rem;
  box-sizing: border-box;
}

.login-panel {
  width: 100%;
  max-width: 920px;
  border-radius: var(--login-radius);
  border: 1px solid var(--login-border);
  background: var(--login-panel-bg);
  box-shadow: var(--login-shadow);
  backdrop-filter: blur(16px);
  -webkit-backdrop-filter: blur(16px);
}

.login-panel__toolbar {
  display: flex;
  justify-content: flex-end;
  padding: 0.75rem 0.75rem 0;
}

@media (min-width: 640px) {
  .login-panel__toolbar {
    padding: 1rem 1rem 0;
  }
}

.login-panel__body {
  display: flex;
  flex-direction: column;
  gap: 0;
  padding: 0 1.25rem 1.5rem;
}

@media (min-width: 640px) {
  .login-panel__body {
    padding: 0 1.75rem 1.75rem;
  }
}

@media (min-width: 1024px) {
  .login-panel__body {
    flex-direction: row;
    align-items: stretch;
    padding: 0 2rem 2rem;
    gap: 0;
  }
}

.login-panel__intro {
  display: flex;
  flex-direction: column;
  gap: 1.25rem;
  padding: 0.25rem 0 1.25rem;
}

@media (min-width: 1024px) {
  .login-panel__intro {
    flex: 1.05;
    min-width: 0;
    padding: 0.5rem 1.5rem 0.5rem 0.25rem;
  }
}

.login-panel__divider {
  display: none;
}

@media (min-width: 1024px) {
  .login-panel__divider {
    display: block;
    width: 1px;
    flex-shrink: 0;
    margin: 0 0.25rem;
    align-self: stretch;
    min-height: 12rem;
    background: linear-gradient(
      180deg,
      transparent 0%,
      var(--login-border) 18%,
      var(--login-border) 82%,
      transparent 100%
    );
    opacity: 0.9;
  }
}

.login-panel__form {
  border-radius: calc(var(--login-radius) - 4px);
  background: transparent;
  border: none;
  padding: 0 0 0.25rem;
}

@media (min-width: 1024px) {
  .login-panel__form {
    flex: 0.95;
    min-width: 0;
    padding: 1.5rem 1.5rem 1.75rem;
    display: flex;
    flex-direction: column;
    justify-content: center;
    background: var(--login-form-well);
    border: 1px solid rgb(15 23 42 / 0.05);
    border-radius: calc(var(--login-radius) - 4px);
  }

  .login-page[data-theme='dark'] .login-panel__form {
    border-color: rgb(255 255 255 / 0.06);
  }
}

.login-brand {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.login-mark {
  display: flex;
  height: 2.5rem;
  width: 2.5rem;
  align-items: center;
  justify-content: center;
  border-radius: 10px;
  background: rgb(244, 72, 30);
  font-size: 0.875rem;
  font-weight: 600;
  color: white;
  box-shadow: 0 1px 2px rgb(15 23 42 / 0.08);
  transition: transform 0.2s ease;
}

.login-page[data-theme='dark'] .login-mark {
  background: rgb(250 250 250);
  color: rgb(24 24 27);
}

.login-mark:hover {
  transform: scale(1.02);
}

.login-brand-title {
  font-size: 0.875rem;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.login-brand-sub {
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--login-muted);
}

.login-headline {
  margin: 0;
  max-width: 20rem;
  font-size: 1.5rem;
  font-weight: 600;
  line-height: 1.25;
  letter-spacing: -0.02em;
}

@media (min-width: 640px) {
  .login-headline {
    font-size: 1.75rem;
    max-width: 22rem;
  }
}

.login-lead {
  margin: 0;
  max-width: 26rem;
  font-size: 0.875rem;
  line-height: 1.65;
  color: var(--login-muted);
}

.login-meta {
  margin: 0;
  padding: 0;
  list-style: none;
  display: flex;
  flex-direction: column;
  gap: 0.75rem;
  font-size: 0.75rem;
  line-height: 1.5;
  color: var(--login-muted);
}

.login-meta li {
  display: flex;
  align-items: flex-start;
  gap: 0.5rem;
}

.login-dot {
  margin-top: 0.35rem;
  height: 0.375rem;
  width: 0.375rem;
  flex-shrink: 0;
  border-radius: 999px;
}

.login-dot--ok {
  background: rgb(16 185 129);
  box-shadow: 0 0 0 3px rgb(16 185 129 / 0.2);
}

.login-dot--muted {
  background: rgb(148 163 184);
}

.login-page[data-theme='dark'] .login-dot--muted {
  background: rgb(113 113 122);
}

.login-code {
  padding: 0.1rem 0.35rem;
  border-radius: 6px;
  background: var(--login-accent-soft);
  font-size: 0.7rem;
  font-family: ui-monospace, SFMono-Regular, Menlo, Monaco, Consolas, monospace;
  color: var(--login-text);
}

.login-footnote {
  margin: 1.25rem 0 0;
  font-size: 0.6875rem;
  color: var(--login-muted);
  text-align: center;
  max-width: 920px;
}

.login-theme-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.5rem;
  height: 2.25rem;
  padding: 0 0.75rem;
  border-radius: 8px;
  border: 1px solid var(--login-border);
  background: var(--login-form-well);
  font-size: 0.75rem;
  font-weight: 500;
  color: var(--login-text);
  cursor: pointer;
  box-shadow: 0 1px 2px rgb(15 23 42 / 0.04);
  transition:
    border-color 0.2s ease,
    background 0.2s ease,
    transform 0.15s ease;
}

.login-theme-btn:hover {
  border-color: rgb(203 213 225);
}

.login-page[data-theme='dark'] .login-theme-btn:hover {
  border-color: rgb(63 63 70);
}

.login-theme-btn:active {
  transform: scale(0.98);
}

.login-theme-thumb {
  width: 0.875rem;
  height: 0.875rem;
  border-radius: 999px;
  background: linear-gradient(135deg, rgb(251 191 36), rgb(249 115 22));
  box-shadow: 0 0 0 2px rgb(255 255 255);
}

.login-page[data-theme='dark'] .login-theme-thumb {
  background: linear-gradient(135deg, rgb(129 140 248), rgb(167 139 250));
  box-shadow: 0 0 0 2px rgb(24 24 27);
}

.login-card-head {
  margin-bottom: 1.5rem;
}

.login-card-title {
  margin: 0 0 0.25rem;
  font-size: 1.125rem;
  font-weight: 600;
  letter-spacing: -0.02em;
}

.login-card-desc {
  margin: 0;
  font-size: 0.8125rem;
  color: var(--login-muted);
}

.login-form :deep(.ant-form-item-label > label) {
  font-weight: 500;
}

.login-input-icon {
  color: rgb(148 163 184);
}

.login-page[data-theme='dark'] .login-input-icon {
  color: rgb(113 113 122);
}

.login-row {
  margin-bottom: 0.5rem;
}

.login-submit-wrap {
  margin-bottom: 0;
  margin-top: 0.5rem;
}

.login-submit-wrap :deep(.ant-btn-primary) {
  font-weight: 500;
  height: 44px;
  border-radius: 10px;
  box-shadow: 0 1px 2px rgb(79 70 229 / 0.2);
  transition:
    transform 0.15s ease,
    box-shadow 0.2s ease;
}

.login-submit-wrap :deep(.ant-btn-primary:not(:disabled):active) {
  transform: scale(0.99);
}
</style>
