.equ    N, 11
.equ    nchar,  8

        .section .text
        .global _start
_start:
        ldr     r1, =buf   @ 作業領域の末尾の次の番地
        add     r1, r1, #nchar
	ldr     r3, =N @reg_dst
	mov     r4, #16
	mov     r9, #0

loop0:

	sub     r1, r1, #1         @ 次の書き込み先
	mov     r7, #'0'           @ 文字 0
	udiv    r5, r3, r4         @ r5商
	mul     r6, r5, r4
	sub     r8, r3, r6         @ r8余り
	cmp     r8, #10
	bmi     under10

	mov     r7, #'A'
	sub     r8, #10
under10:
	add     r9, r9, #1	   @ 出力文字数
	cmp		 r9, #9
	beq     end
	add     r7, r7, r8
	strb    r7, [r1]           @ 作業領域に1文字書き込む
	mov     r3, r5
	mov     r10, r5
	b       loop0

end:

       @ writeシステムコールを呼び出す。そのあと終了
        mov     r7, #4            @ システムコール番号
        mov     r0, #1            @ 標準出力
	mov     r2, r9          @ 改行を含めた長さ
        swi     #0                @ システムコール番号
       @exit
    mov     r7, #1            @ システムコール
	mov     r0, #0            @ 終了コード
	swi     #0

        .section .data
buf:    .space  8, 0          @ nchar文字分の領域
        .byte   0x0a              @ 改行
