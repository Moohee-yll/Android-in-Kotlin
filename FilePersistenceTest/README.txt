主要功能：实现对安卓系统的文件读取。
当前版本的主要实现技术：利用URI对文件进行增删改查
当前版本实现的情况：可以在任意目录下创建和读取文件；可以删除任意文件；
修改文件存在以下问题：
1、对于快速访问“文档”下的文件，其返回的URI格式是 java.lang.IllegalArgumentException: Media is read-only，显示错误：Error!  java.lang.IllegalArgumentException: Media is read-only
2、对于某些文档，写入的效果是“在原本的内容前面加上了一些文本，没有覆盖”，例如已有“3456”的文件，想改为2222，结果是22223456；再写入1111，结果是11113456