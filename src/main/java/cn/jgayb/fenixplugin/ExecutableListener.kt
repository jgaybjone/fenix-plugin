package cn.jgayb.fenixplugin

interface ExecutableListener {
    fun isWriteAction(): Boolean
}

interface ClickableListener : ExecutableListener {
    fun clicked()
}