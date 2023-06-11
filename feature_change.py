import pyworld as pw
import numpy as np
import soundfile as sf
import glob
import os
import random
import sys


file1 = sys.argv[1]
file2 = sys.argv[2]
param = sys.argv[3]

data, fs = sf.read(file1)
f0, t = pw.dio(data, fs)  #  提取基频
sp = pw.cheaptrick(data, f0, t, fs)  # 提取频谱包络
ap = pw.d4c(data, f0, t, fs)  # 提取非周期性指数

data1, fs1 = sf.read(file2)
f01, t1 = pw.dio(data1, fs1)  #  提取基频
sp1 = pw.cheaptrick(data1, f01, t1, fs1)  # 提取频谱包络
ap1 = pw.d4c(data1, f01, t1, fs1)  # 提取非周期性指数

#保留每帧前六分之五的内容，并扩展到整个帧
modify_sp = np.zeros_like(sp)
for f in range(modify_sp.shape[1]):
    modify_sp[:, f] = sp[:, int(f/1.2)]

modify_sp1 = np.zeros_like(sp1)
for f in range(modify_sp1.shape[1]):
    modify_sp1[:, f] = sp1[:, int(f/1.2)]

ran_num = 100   #语音分为100段
f0_per_size = int(1100/ran_num)  #每段11帧

#检测参数文件是否存在，不存在则新建
mul_num = []
if os.path.exists(param):
    mul_num = np.load(param).tolist()
else:
    #计算修改每个段使用的倍率
    index_num = np.random.randint(0,len(sp[0])-1,size=f0_per_size)
    for i in range(0,len(sp1),f0_per_size):
        sum = 0
        for j in range(f0_per_size):
            sum = sum+sp1[i+j,index_num[j]]
        mul_num.append((sum/sp1[i,index_num[j]]/f0_per_size)%0.8+0.6)
    np.save(param,np.array(mul_num))
    
#print(len(mul_num))
#print(mul_num)

#按照每个段对基频和频谱包络修改
for i in range(ran_num):
    for j in range(f0_per_size):
        f0[j+i*f0_per_size]=f0[j+i*f0_per_size]*mul_num[i]
        modify_sp[j+i*f0_per_size]=modify_sp[j+i*f0_per_size]+sp1[j+i*f0_per_size]*0.002*mul_num[i]
        modify_sp[j+i*f0_per_size]=modify_sp[j+i*f0_per_size]*mul_num[i]

for i in range(ran_num):
    for j in range(f0_per_size):
        f01[j+i*f0_per_size]=f0[j+i*f0_per_size]*mul_num[i]
        modify_sp1[j+i*f0_per_size]=modify_sp1[j+i*f0_per_size]+sp[j+i*f0_per_size]*0.002*mul_num[i]
        modify_sp1[j+i*f0_per_size]=modify_sp1[j+i*f0_per_size]*mul_num[i]

#合成新的语音
my_voice = pw.synthesize(f0, modify_sp, ap, fs)
my_voice1 = pw.synthesize(f01, modify_sp1, ap1, fs1)

sf.write(file1,my_voice,fs)
sf.write(file2,my_voice1,fs1)
