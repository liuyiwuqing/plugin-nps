<script lang="ts" setup>
import {Toast, VPageHeader} from "@halo-dev/components";
import apiClient from "@/utils/api-client";
import {reactive} from "vue";

let operationData = reactive({
  userExist: false,
  userInfo: {
    userName: '',
    userPassword: ''
  }
})

const getNpsUser = async () => {
  const {data} = await apiClient.get(`/apis/nps.lywq.site/v1alpha1/plugins/PluginNps/getNpsUser`);
  if (data.status === 0) {
    operationData.userInfo = data.data;
    operationData.userExist = true;
  } else {
    operationData.userInfo.userName = data.data.userName;
    operationData.userExist = false;
    Toast.warning(data.msg);
  }
};

const loginOrRegister = async () => {
  if (operationData.userExist) {
    const {data} = await apiClient.post(
      `/apis/nps.lywq.site/v1alpha1/plugins/PluginNps/userLogin?userName=${operationData.userInfo.userName}&userPassword=${operationData.userInfo.userPassword}`
    );

    if (data.status === 0) {
      Toast.success(data.msg);
      window.open(data.data, '_blank');
    } else {
      operationData.userExist = false;
      Toast.warning(data.msg);
    }
  } else {
    const {data} = await apiClient.post(
      `/apis/nps.lywq.site/v1alpha1/plugins/PluginNps/userRegister?userName=${operationData.userInfo.userName}&userPassword=${operationData.userInfo.userPassword}`
    );

    if (data.status === 0) {
      operationData.userInfo = data.data;
      operationData.userExist = true;
      Toast.success(data.msg);
      await loginOrRegister();
    } else {
      Toast.warning(data.msg);
    }
  }
};
getNpsUser();
</script>
<template>
  <VPageHeader title="nps管理">
    <template #icon>
    </template>
  </VPageHeader>
  <div class="nps-container">
    <div class="nps-form-group">
      <div class="nps-title">NPS账号{{ operationData.userExist ? '登录' : '注册' }}</div>
      <form @submit.prevent="loginOrRegister">
        <div>
          <label for="loginUserName">账号：</label>
          <input type="text" id="loginUserName" v-model="operationData.userInfo.userName" disabled>
        </div>
        <div v-show="!operationData.userExist">
          <label for="loginUserPassword">密码：</label>
          <input type="text" id="loginUserPassword" v-model="operationData.userInfo.userPassword"
                 placeholder="首次激活请输入密码">
        </div>
        <button class="nps-button" type="submit">{{ operationData.userExist ? '登录' : '注册' }}</button>
      </form>
    </div>
  </div>
</template>
<style>
.nps-container {
  width: 100%;
  padding-right: .5rem;
  padding-left: .5rem;
  padding-top: 10rem;
}

.nps-title {
  font-size: 30px;
  font-weight: bold;
  text-align: center;
  line-height: 80px;
}

.nps-form-group {
  max-width: 400px;
  margin: 0 auto;
  padding: 20px;
  border: 1px solid #ccc;
  border-radius: 10px;
}

.nps-form-group label {
  display: block;
  margin-bottom: 5px;

}

.nps-form-group input[type='text'], .nps-form-group input[type='password'] {
  padding: 10px;
  border-radius: 5px;
  border: 1px solid #ccc;
  width: 100%;
}

.nps-form-group button {
  padding: 10px;
  background: #007bff;
  border: none;
  color: #fff;
  cursor: pointer;
  border-radius: 5px;
  width: 100%;
}

.nps-form-group button:hover {
  background: #0069d9;
}

.nps-button {
  margin-top: 20px;
  margin-bottom: 5px;
}

</style>
