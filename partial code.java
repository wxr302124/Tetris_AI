////////
import java.util.*;
import java.io.*;

////////
public class Q4D0123456789 extends Tetris {
	// enter your student id here
	public String id = new String("Q4D1500012906");
	
	public static final boolean pieceShape[][][] = {
		{{false,false,false,false},
			{true,true,true,true},
			{false,false,false,false},
         	{false,false,false,false}},
		{{false,false,false,false},
            { true, true,false,false},
            {false, true, true,false},
            {false,false,false,false}},
		{{false,false,false,false},
        	{false,false, true, true},
         	{false, true, true,false},
         	{false,false,false,false}},
		{{false,false,false,false},
         	{ true, true, true,false},
            { true,false,false,false},
            {false,false,false,false}},
        {{false,false,false,false},
            {false, true, true, true},
            {false,false,false, true},
            {false,false,false,false}},
        {{false,false,false,false},
            { true, true, true,false},
            {false, true,false,false},
            {false,false,false,false}},
        {{false,false,false,false},
            {false, true, true,false},
            {false, true, true,false},
            {false,false,false,false}}
	};
	public static final int I = 0, Z = 1, S = 2, L = 3,
			J = 4, T = 5, O = 6; // ���ַ��������
	public static final int rotationCount[] = {2, 2, 2, 4, 4, 4, 1}; // ������ת����״��
	public static final int left_edge[][] = { // ÿһ����ת״̬�����Ե
			{0, 1}, 
			{0, 1},
			{1, 1},
			{0, 1, 1, 1},
			{1, 1, 0, 1},
			{0, 1, 1, 1},
			{1}
	};
	public static final double MIN = -1000000.0;
	private boolean board[][];
	private boolean piece[][];
	private int shape;
	private int piece_x, old_piece_x;
	private int piece_y, old_piece_y;
	private PieceOperator path[]; // �洢���·��
	private PieceOperator tmp_path[];
	private int path_cnt, tmp_path_cnt, path_i;;
	private double best_value;
	
	// ���캯��
	Q4D0123456789(){
		path = new PieceOperator[h*w*4];
		tmp_path = new PieceOperator[h*w*4];
		path_cnt = 0;
		tmp_path_cnt = 0;
		path_i = 0;
		best_value = MIN;
	}
	
	// Dellacherie����������Ҫ�õ���һЩ����
	private int getLandingHeight(){
		return piece_y - 1; //ƽ���߶ȴ�Լ�൱��-1
	}
	private int getErodedPieceCellsMetric(){
		int erodedRows = 0, erodedShapes = 0; // �����������鹱����
		for (int y = 0; y < h - nBufferLines; y++) {
			boolean full = true;
			for (int x = 0; x < w; x++) {
				if (!board[y][x]) {
					full = false;
					break;
				}
			}
			if (full){
				if ((piece_y - y <= 3) && (piece_y - y >= 0)){
					for (int i = 0; i < 4; i++){
						if (piece[piece_y - y][i])    
							erodedShapes++;
					}
				}
				erodedRows++;
			}
		}
		return erodedRows*erodedShapes;
	}
	private int getBoardRowTransitions(){
		int transitions = 0;
		for (int y = 0; y < h - nBufferLines; y++){
			if (!board[y][0])
				transitions++; // ǽҲ��Ϊ����
			for (int x = 0; x < w - 1; x++){
				if (board[y][x] != board[y][x + 1])
					transitions++;
			}
			if (!board[y][w - 1])
				transitions++;
		}
		return transitions;
	}
	private int getBoardColTransitions(){
		int transitions = 0;
		for (int x = 0; x < w; x++){
			if (!board[0][x])
				transitions++; // ǽҲ��Ϊ����
			for (int y = 0; y < h - nBufferLines - 1; y++){
				if (board[y][x] != board[y + 1][x])
					transitions++;
			}
		}
		return transitions;
	}
	private int getBoardBuriedHoles(){
		int holes = 0;
		for (int x = 0; x < w; x++){
			int y = h - nBufferLines - 1;
			while (y >= 0 && !board[y][x])
				y--;
			while (y >= 0){
				if (!board[y][x])
					holes++;
				y--;
			}
		}
		return holes;
	}
	private int getBoardWells(){
		int depth = 0, sum = 0;
		for (int y = h - nBufferLines - 1; y >= 0; y--){
			if (!board[y][0] && board[y][1]){ // ǽ
				depth++;
				sum += depth;
			}
			else
				depth = 0;
		}
		for (int x = 1; x < w - 1; x++){
			for (int y = h - nBufferLines - 1; y >= 0; y--){
				if (!board[y][x]){
					if (board[y][x - 1] && board[y][x + 1]){
						depth++;
						sum += depth;
					}
				}
				else
					depth = 0;
			}
		}
		for (int y = h - nBufferLines - 1; y >= 0; y--){
			if (!board[y][w - 1] && board[y][w - 2]){ // ǽ
				depth++;
				sum += depth;
			}
			else
				depth = 0;
		}
		return sum;
	}
	
	// Dellacherie���ֺ���
	private double Evaluation(){
		double evalue = 0;
		int lh = getLandingHeight(); // ���߶�
		int epcm = getErodedPieceCellsMetric(); // ������*���鹱����
		int brt = getBoardRowTransitions(); // �б任��
		int bct = getBoardColTransitions(); // �б任��
		int bbh = getBoardBuriedHoles(); // �ն���
		int bw = getBoardWells(); // ���ĸ���
		
		evalue = -4.50016*lh + 3.41813*epcm - 3.21789*brt - 9.34870*bct - 7.89927*bbh - 3.38560*bw;	
		return evalue;
	}
	
	// �жϵ�ǰ�������״
	private int Shape(boolean piece[][]){
		for (int i = 0; i < 7; i++){
			boolean thisShape = true;
			for (int j = 0; j < 4; j++)
				for (int k = 0; k < 4; k++){
					if (piece[j][k] != pieceShape[i][j][k]){
						thisShape = false;
						break;
					}
				}
			if (thisShape)
				return i;
		}
		return -1;
	}
	
	//�ж��Ƿ�����������һ������ƶ���px��py�ǵ�ǰ���Եķ������Ͻ�λ��
	private boolean canPieceMove(PieceOperator op, int px, int py){
		removePieceFromBoard(); // �ȴӳ����аѷ������������ж�
		switch(op){
		case ShiftLeft: px--; break;
		case ShiftRight: px++; break;
		case Drop: py--; break;
		default:
			return false;
		}
		
		boolean deployable = true; //�Ƿ�����ƶ�
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				if (!piece[y][x]) 
					continue;
				if (px + x < 0 || px + x >= w
				 || py - y < 0 || py - y >= h
				 || board[py - y][px + x]) {
					deployable = false;
					break;
				}
			}
		}
		return deployable;
	}
	
	// �ѷ�����ʱ�ӳ���������
	private void removePieceFromBoard(){
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				if (piece[y][x]) 
					board[piece_y - y][piece_x + x] = false;
			}
		}
	}
	
	//�ѷ�����ʱ���뵽���أ�����piece_x��piece_y
	private void addPieceToBoard(int px, int py){
		old_piece_x = piece_x;
		old_piece_y = piece_y;
		piece_x = px;
		piece_y = py;
		for (int x = 0; x < 4; x++) {
			for (int y = 0; y < 4; y++) {
				if (piece[y][x]) 
					board[piece_y - y][piece_x + x] = true;
			}
		}
	}
	
	// ����Ϸ�ĵ�0�п�ʼ����������Ե�ڸ��У������λ�ó��԰ڷŵ�ǰ��ת״̬�ط���
	private void evaluateShape(int rotation_cnt){
		double cur_value;
		for (int x = 0; x < w; x++){
			piece_x = old_piece_x;
			piece_y = old_piece_y;
			for (tmp_path_cnt = 0; tmp_path_cnt < rotation_cnt; tmp_path_cnt++)
				tmp_path[tmp_path_cnt] = PieceOperator.Rotate;
			int i = piece_x; // i�Ƿ��鳢�Ե�λ�õĺ�����
			if (x < piece_x + left_edge[shape][rotation_cnt]){
				while (canPieceMove(PieceOperator.ShiftLeft, i, piece_y)
						&& (i + left_edge[shape][rotation_cnt] > x)){
					i--;
					tmp_path[tmp_path_cnt++] = PieceOperator.ShiftLeft;
				}
			}
			else if (x > piece_x + left_edge[shape][rotation_cnt]){
				while (canPieceMove(PieceOperator.ShiftRight, i, piece_y)
						&& (i + left_edge[shape][rotation_cnt] < x)){
					i++;
					tmp_path[tmp_path_cnt++] = PieceOperator.ShiftRight;			
				}
			}
			if (i + left_edge[shape][rotation_cnt] != x) // �޷��ƶ���Ŀ��λ�����Ϸ�
				continue;
			
			int j = piece_y; // j�Ƿ��鳢�Ե�λ�õ�������
			while (canPieceMove(PieceOperator.Drop, i, j)){
				j--;
				tmp_path[tmp_path_cnt++] = PieceOperator.Drop;			
			}
			addPieceToBoard(i, j);
			cur_value = Evaluation();
			if (cur_value > best_value){
				best_value = cur_value;
				path_cnt = tmp_path_cnt;
				for (int k = 0; k < tmp_path_cnt; k++)
					path[k] = tmp_path[k];
			}
			removePieceFromBoard();
		}
	}
	
	// ####
	public PieceOperator robotPlay() {
		PieceOperator op = PieceOperator.Keep;
		board = getBoard();
		piece = getPiece();
		piece_x = getPieceX();
		piece_y = getPieceY();
		
		if (!canPieceMove(PieceOperator.Drop, piece_x, piece_y)) // �޷������ƶ�
			return op;
		
		shape = Shape(piece);
		if ((piece_y == h - 1) && (piece_x == w/2) && shape != -1){ // ��ʼ״̬
			old_piece_x = piece_x;
			old_piece_y = piece_y;
			path_cnt = 0;
			path[0] = PieceOperator.Keep;
			path_i = 0;
			best_value = MIN;
			
			removePieceFromBoard();
			boolean new_piece[][] = new boolean[4][4]; // �����ת���״̬
			for (int i = 0; i < rotationCount[shape]; i++){
				piece_x = old_piece_x;
				piece_y = old_piece_y;
				if (i >= 1){ // ��ת
					for (int y = 0; y < 4; y++) {
						for (int x = 0; x < 4; x++) {
							new_piece[y][x] = piece[x][3 - y];
						}
					}
					for (int y = 0; y < 4; y++) { // ����piece
						for (int x = 0; x < 4; x++) {
							piece[y][x] = new_piece[y][x];
						}
					}
				}
				evaluateShape(i);
			}
		}
		
		if (path_i < path_cnt){
			op = path[path_i];
			path_i++;
		}
		return op;

/*		// this is a random player ...
 *		switch (new Random().nextInt(4)) {
 *			case 0: return PieceOperator.ShiftLeft;
 *			case 1: return PieceOperator.ShiftRight;
 *			case 2: return PieceOperator.Rotate;
 *			case 3: return PieceOperator.Drop;
 *		}
 *		
 *		return PieceOperator.Keep;
 *	}
 */
	}
}
