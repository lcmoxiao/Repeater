package com.example.repeater.elf_home

class ElfData {
    var length: String
    var desc: String

    constructor(_length: String, _desc: String) {
        length = _length
        desc = _desc
    }

    constructor(_length: Int, _desc: Int) {
        length = _length.toString()
        desc = _desc.toString()
    }
}