	.section .data
	@大域変数の定義
_Pi_var_x:
	.word 0
_Pi_var_y:
	.word 0
_Pi_var_i:
	.word 0
_Pi_var_answer:
	.word 0
_Pi_var_buf:
	.space	8, 0
	.byte	0x0a
	.section .text
	.global _start
_start:
	@式をコンパイルした命令列
	ldr r0, =#0
	ldr r1, =_Pi_var_i
	str r0, [r1, #0]
	ldr r0, =#15
	ldr r1, =_Pi_var_x
	str r0, [r1, #0]
	ldr r0, =_Pi_var_x
	ldr r0, [r0, #0]
	cmp r0, #0
	beq L0
	ldr r0, =_Pi_var_x
	ldr r0, [r0, #0]
	str r1, [sp, #-4]!
	mov r1, r0
	mvn r0, r1
	add r0, r0, #1
	ldr r1, [sp], #4
	ldr r1, =_Pi_var_y
	str r0, [r1, #0]
	b L1
L0:
	ldr r0, =#128
	ldr r1, =_Pi_var_y
	str r0, [r1, #0]
L1:
	ldr r0, =_Pi_var_i
	ldr r0, [r0, #0]
	cmp r0, #0
	bne L2
	ldr r0, =_Pi_var_i
	ldr r0, [r0, #0]
	str r1, [sp, #-4]!
	mov r1, r0
	ldr r0, =#1
	add r0, r1, r0
	ldr r1, [sp], #4
	ldr r1, =_Pi_var_i
	str r0, [r1, #0]
	ldr r0, =_Pi_var_y
	ldr r0, [r0, #0]
	str r1, [sp, #-4]!
	mov r1, r0
	ldr r0, =#2
	add r0, r1, r0
	ldr r1, [sp], #4
	ldr r1, =_Pi_var_y
	str r0, [r1, #0]
L2:
	ldr r0, =_Pi_var_y
	ldr r0, [r0, #0]
	str r1, [sp, #-4]!
	mov r1, r0
	ldr r0, =#1
	sub r0, r1, r0
	ldr r1, [sp], #4
	ldr r1, =_Pi_var_answer
	str r0, [r1, #0]
	ldr r0, =#1
	str r1, [sp, #-4]!
	mov r1, r0
	ldr r0, =#10
	add r0, r1, r0
	ldr r1, [sp], #4
	ldr r1, =_Pi_var_buf
	add r1, r1, #8
	mov r3, r0
	mov r4, #16
	mov r9, #0
L3:
	add r9, r9, #1
	cmp r9, #9
	beq L5
	sub r1, r1, #1
	mov r7, #48
	udiv r5, r3, r4
	mul r6, r5, r4
	sub r8, r3, r6
	cmp r8, #10
	bmi L4
	mov r7, #65
	sub r8, #10
L4:
	add r7, r7, r8
	strb r7, [r1]
	mov r3, r5
	mov r10, r5
	b L3
L5:
	mov r7, #4
	mov r0, #0
	mov r2, r9
	swi #0
	@ EXITシステムコール
	ldr r0, =_Pi_var_answer
	ldr r0, [r0, #0]
	mov r7, #1
	swi #0
