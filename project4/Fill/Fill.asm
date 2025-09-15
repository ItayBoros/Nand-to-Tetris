// This file is part of www.nand2tetris.org
// and the book "The Elements of Computing Systems"
// by Nisan and Schocken, MIT Press.
// File name: projects/4/Fill.asm

// Runs an infinite loop that listens to the keyboard input. 
// When a key is pressed (any key), the program blackens the screen,
// i.e. writes "black" in every pixel. When no key is pressed, 
// the screen should be cleared.

//// Replace this comment with your code.
(LOOP)
  @KBD
  D=M
  @BLACK
  D;JNE
  @WHITE
  D;JEQ

(BLACK)
  @ROW_COUNTER_BLACK
  M=0
  //MAKES THE SCREEN BLACK

  (BLACKLOOP)
    //CHECK IF WE FILLED ENTIRE SCREEN
    @ROW_COUNTER_BLACK
    D=M
    @8192
    D=D-A
    @LOOP
    D;JGE 

    //COUNTIUNE TO MAKE BLACK LINES
    @SCREEN
    D=A
    @ROW_COUNTER_BLACK
    A=D+M
    M=-1

    //INCREACE ROW COUNTER BY 1
    @ROW_COUNTER_BLACK
    M=M+1
    
    //REPEAT THE LOOP
    @BLACKLOOP
    0;JMP


(WHITE)
  //MAKES THE SCREEN WHITE
  @ROW_COUNTER_WHITE
  M=0
  //MAKES THE SCREEN BLACK

  (WHITELOOP)
    //CHECK IF WE OVER LINE 255
    @ROW_COUNTER_WHITE
    D=M
    @8192
    D=D-A
    @LOOP
    D;JGE

    //COUNTIUNE TO MAKE WHITE LINES
    @SCREEN
    D=A
    @ROW_COUNTER_WHITE
    A=D+M
    M=0

    //INCREACE ROW COUNTER BY 1
    @ROW_COUNTER_WHITE
    M=M+1
    
    //REPEAT THE LOOP
    @WHITELOOP
    0;JMP


    