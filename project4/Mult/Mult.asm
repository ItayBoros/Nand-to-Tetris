// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/4/Mult.asm

// Multiplies R0 and R1 and stores the result in R2.
// (R0, R1, R2 refer to RAM[0], RAM[1], and RAM[2], respectively.)
// The algorithm is based on repetitive addition.

//// Replace this comment with your code.

//CREATE NEW VAR i THAT EQUALS TO R1
@R1
D=M
@i
M=D

//R[2] == 0
@R2
M=0

(LOOP)
  //IF (I == 0) GOTO END
  @i
  D=M
  @END
  D;JEQ
  //ADD TO R[2] THE VALUE OF R[0] ONE MORE TIME
  @R0
  D=M
  @R2
  M=D+M
  //DECREASE i BY 1 
  @i
  M=M-1 //i=i-1
  
  //GO TO LOOP
  @LOOP
  0;JMP

(END)
  @END
  0;JMP

