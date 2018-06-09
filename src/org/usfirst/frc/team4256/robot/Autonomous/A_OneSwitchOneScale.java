package org.usfirst.frc.team4256.robot.Autonomous;

import org.usfirst.frc.team4256.robot.Parameters;
import org.usfirst.frc.team4256.robot.R_Clamp;
import org.usfirst.frc.team4256.robot.R_Drivetrain;
import org.usfirst.frc.team4256.robot.Elevators.R_Combined;

import com.cyborgcats.reusable.V_PID;
import com.cyborgcats.reusable.Autonomous.P_Bezier;
import com.cyborgcats.reusable.Autonomous.V_Events;
import com.cyborgcats.reusable.Autonomous.V_Leash;
import com.cyborgcats.reusable.Autonomous.Odometer;

public class A_OneSwitchOneScale implements Autonomous{
	public final FieldPieceConfig switchTarget, scaleTarget;
	public final StartingPosition startingPosition;
	private final Odometer odometer;

	public V_Leash leash;
	public V_Events events;
	public double initOdometerPosX = 0.0;

	public A_OneSwitchOneScale(final int startingPosition, final String gameData, final Odometer odometer) {
		//{organize initialization data}
		switchTarget = gameData.charAt(0) == 'L' ? FieldPieceConfig.LEFT : FieldPieceConfig.RIGHT;//SWITCH
		scaleTarget = gameData.charAt(1) == 'L' ? FieldPieceConfig.LEFT : FieldPieceConfig.RIGHT;//SCALE
		switch (startingPosition) {//ROBOT
		case(0):this.startingPosition = StartingPosition.LEFT;  break;
		case(1):this.startingPosition = StartingPosition.CENTER;break;
		case(2):this.startingPosition = StartingPosition.RIGHT; break;
		default:this.startingPosition = StartingPosition.CENTER;break;
		}

		this.odometer = odometer;

		switch (this.startingPosition) {
		case LEFT:  useLeash_left();
					useEvents_left();  break;
		case CENTER:useLeash_center();
					useEvents_center();break;
		case RIGHT: useLeash_right();
					useEvents_right(); break;
		default:    useLeash_center();
					useEvents_center();break;
		}
	}

	public void run(final R_Drivetrain swerve, final R_Clamp clamp, final R_Combined elevator) {
		events.check(leash.getIndependentVariable());
  		final double spin = events.execute(clamp, elevator, swerve.gyro);

		//run path processing only if odometer values are new
  		if (odometer.newX() || odometer.newY()) {
  			//get most recent odometer values
			final double actualX = odometer.getX(true),
						 actualY = odometer.getY(true);

			//ensure that the desired position stays a leash length away
			leash.maintainLength(actualX, actualY);

			//get desired position and compute error components
			final double desiredX = leash.getX(),
						 desiredY = leash.getY();
			final double errorX = desiredX - actualX,
						 errorY = desiredY - actualY;

			//use error components to compute commands that swerve understands
			final double errorDirection = Math.toDegrees(Math.atan2(errorX, errorY));
			final double errorMagnitude = Math.hypot(errorX, errorY);
			double speed = V_PID.get("zed", errorMagnitude);//DO NOT use I gain with this because errorMagnitude is always positive
			if (speed > 0.7) speed = 0.7;

			swerve.holonomic_encoderIgnorant(errorDirection, speed, spin);
  		}
	}

	public double initX() {return initOdometerPosX;}

	//------------------------------------------------------------------------------------------
	private void useLeash_left() {
		initOdometerPosX = leftStartX;

		if (switchTarget.equals(FieldPieceConfig.LEFT) && scaleTarget.equals(FieldPieceConfig.LEFT)) {
			//{easy switch then easy scale}
			final P_Bezier a = new P_Bezier(leftStartX, initY, -110, 93, -93, 93, -switchX, switchY, 0.0);//get to easy switch
			final P_Bezier b = new P_Bezier(-switchX, switchY, -115, 202, -91, 228, -cubeX, cubeY, 1.0);//get to new cube
			final P_Bezier c = new P_Bezier(-cubeX, cubeY, -80, 252, -70, 270, -scaleX, scaleY, 2.0);//get to easy scale

			final P_Bezier[] path = new P_Bezier[] {a, b, c};
			leash = new V_Leash(path, /*leash length*/1.5, /*growth rate*/0.1);

		}else if (switchTarget.equals(FieldPieceConfig.LEFT)) {
			//{easy switch then hard scale}
			final P_Bezier a = new P_Bezier(leftStartX, initY, -110, 93, -93, 93, -switchX, switchY, 0.0);//get to easy switch
			final P_Bezier b = new P_Bezier(-switchX, switchY, -115, 202, -91, 228, -cubeX, cubeY, 1.0);//get to new cube
			final P_Bezier c = new P_Bezier(-cubeX, cubeY, -30, 289, 94, 190, scaleX, scaleY, 2.0);//get to hard scale

			final P_Bezier[] path = new P_Bezier[] {a, b, c};
			leash = new V_Leash(path, /*leash length*/1.5, /*growth rate*/0.1);

		}else if (scaleTarget.equals(FieldPieceConfig.LEFT)) {
			//{easy scale then hard switch}
			final P_Bezier a = new P_Bezier(leftStartX, initY, -120, 215, -86, 242, -scaleX, scaleY, 0.0);//get to easy scale
			final P_Bezier b = new P_Bezier(-scaleX, scaleY, -91, 193, 50, 277, cubeX, cubeY, 1.0);//get to new cube/hard switch

			final P_Bezier[] path = new P_Bezier[] {a, b};
			leash = new V_Leash(path, /*leash length*/1.5, /*growth rate*/0.1);

		}else {
			//{hard switch then hard scale}
			final P_Bezier a = new P_Bezier(leftStartX, initY, -112, 140, -166, 268, 22, 255, 0.0);
			final P_Bezier b = new P_Bezier(22, 255, 41, 251, 55, 235, cubeX, cubeY, 1.0);//get to new cube/hard switch
			final P_Bezier c = new P_Bezier(cubeX, cubeY, 82, 240, 73, 264, scaleX, scaleY, 2.0);//get to hard scale

			final P_Bezier[] path = new P_Bezier[] {a, b, c};
			leash = new V_Leash(path, /*leash length*/1.5, /*growth rate*/0.1);
		}
	}
	
	private void useEvents_left() {
		if (switchTarget.equals(FieldPieceConfig.LEFT) && scaleTarget.equals(FieldPieceConfig.LEFT)) {
			// at 1.0, reaches easy switch; at 2.0, reaches new cube; at 3.0, reaches easy scale
			final int[][] instructions = new int[][] {//TODO only takes care of stuff up until switch
				{0, 3, 0, 0},
				{3, Parameters.ElevatorPresets.SWITCH.height(), 0, 0},
				{1, Parameters.ElevatorPresets.SWITCH.height(), 0, 0}
			};
			
			
			final double[] triggers = new double[] {0.3, 0.7, 0.9};
			events = new V_Events(V_Events.getFromArray(instructions), triggers);

		}else if (switchTarget.equals(FieldPieceConfig.LEFT)) {
			// at 1.0, reaches easy switch; at 2.0, reaches new cube; at 3.0, reaches hard scale
			final int[][] instructions = new int[][] {//TODO only takes care of stuff up until switch
				{0, 3, 0, 0},
				{3, Parameters.ElevatorPresets.SWITCH.height(), 0, 0},
				{1, Parameters.ElevatorPresets.SWITCH.height(), 0, 0}
			};
			

			final double[] triggers = new double[] {0.3, 0.7, 0.9};
			events = new V_Events(V_Events.getFromArray(instructions), triggers);

		}else if (scaleTarget.equals(FieldPieceConfig.LEFT)) {
			// at 1.0, reaches easy scale; at 2.0, reaches new cube/hard switch
			final int[][] instructions = new int[][] {
				{4, 3, 0, 5},
				{3, 3, 0, 5},
				{4, Parameters.ElevatorPresets.SCALE_HIGH.height(), 0, 3},
				{4, Parameters.ElevatorPresets.SCALE_HIGH.height(), 0, 1},
				{1, Parameters.ElevatorPresets.SCALE_HIGH.height(), 0, 0}
			};
			

			final double[] triggers = new double[] {0.1, 0.2, 0.6, 0.8, 1.0};
			events = new V_Events(V_Events.getFromArray(instructions), triggers);

		}else {
			// at 1.0, almost to hard switch; at 2.0, reaches new cube/hard switch; at 3.0, reaches hard scale
			final int[][] instructions = new int[][] {//TODO only takes care of stuff up until switch
				{0, 3, 180, 1},
				{3, Parameters.ElevatorPresets.SWITCH.height(), 180, 1},
				{1, Parameters.ElevatorPresets.SWITCH.height(), 180, 1}
			};

			
			final double[] triggers = new double[] {0.3, 0.6, 1.9};
			events = new V_Events(V_Events.getFromArray(instructions), triggers);
		}
	}
	//------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------
	private V_Leash useLeash_center() {
		initOdometerPosX = centerStartX;
		
		P_Bezier bezier;

		if (switchTarget.equals(FieldPieceConfig.LEFT)) bezier = new P_Bezier(centerStartX, initY, -30, 82, -52, 60, -switchX, switchY, 0.0);//get to left switch
		else bezier = new P_Bezier(centerStartX, initY, 30, 82, 52, 60, switchX, switchY, 0.0);//get to right switch
		
		leash = new V_Leash(new P_Bezier[] {bezier}, /*leash length*/1.5, /*growth rate*/0.1);
		return leash;
	}

	private V_Events useEvents_center() {
		// at 1.0, reaches switch
		final int[][] instructions = new int[][] {
			{4, 3, 0, 5},
			{3, Parameters.ElevatorPresets.SWITCH.height(), 0, 5},
			{1, Parameters.ElevatorPresets.SWITCH.height(), 0, 5}
		};
		
		events = new V_Events(V_Events.getFromArray(instructions), new double[] {0.1, 0.2, 1.0});
		return events;
	}
	//------------------------------------------------------------------------------------------
	//------------------------------------------------------------------------------------------
	private void useLeash_right() {
		initOdometerPosX = rightStartX;

		if (switchTarget.equals(FieldPieceConfig.RIGHT) && scaleTarget.equals(FieldPieceConfig.RIGHT)) {
			//{easy switch then easy scale}
			final P_Bezier a = new P_Bezier(rightStartX, initY, 116, 89, 99, 89, switchX, switchY, 0.0);//get to easy switch
			final P_Bezier b = new P_Bezier(switchX, switchY, 115, 202, 91, 228, cubeX, cubeY, 1.0);//get to new cube
			final P_Bezier c = new P_Bezier(cubeX, cubeY, 80, 252, 70, 270, scaleX, scaleY, 2.0);//get to easy scale

			final P_Bezier[] path = new P_Bezier[] {a, b, c};
			leash = new V_Leash(path, /*leash length*/1.5, /*growth rate*/0.1);

		}else if (switchTarget.equals(FieldPieceConfig.RIGHT)) {
			//{easy switch then hard scale}
			final P_Bezier a = new P_Bezier(rightStartX, initY, 116, 89, 99, 89, switchX, switchY, 0.0);//get to easy switch
			final P_Bezier b = new P_Bezier(switchX, switchY, 115, 202, 91, 228, cubeX, cubeY, 1.0);//get to new cube
			final P_Bezier c = new P_Bezier(cubeX, cubeY, 30, 289, -94, 190, -scaleX, scaleY, 2.0);//get to hard scale

			final P_Bezier[] path = new P_Bezier[] {a, b, c};
			leash = new V_Leash(path, /*leash length*/1.5, /*growth rate*/0.1);

		}else if (scaleTarget.equals(FieldPieceConfig.RIGHT)) {
			//{easy scale then hard switch}
			final P_Bezier a = new P_Bezier(rightStartX, initY, 120, 215, 86, 242, scaleX, scaleY, 0.0);//get to easy scale
//			final P_Bezier b = new P_Bezier(scaleX, scaleY, 91, 193, -50, 277, -cubeX, cubeY, 1.0);//get to new cube/hard switch

			final P_Bezier[] path = new P_Bezier[] {a/*, b*/};
			leash = new V_Leash(path, /*leash length*/1.5, /*growth rate*/0.1);
		}else {
			//{hard switch then hard scale}
			final P_Bezier a = new P_Bezier(rightStartX, initY, 112, 140, 166, 268, -22, 255, 0.0);
			final P_Bezier b = new P_Bezier(-22, 255, -41, 251, -55, 235, -cubeX, cubeY, 1.0);//get to new cube/hard switch
			final P_Bezier c = new P_Bezier(-cubeX, cubeY, -82, 240, -73, 264, -scaleX, scaleY, 2.0);//get to hard scale

			final P_Bezier[] path = new P_Bezier[] {a, b, c};
			leash = new V_Leash(path, /*leash length*/1.5, /*growth rate*/0.1);
		}
	}

	private void useEvents_right() {
		if (switchTarget.equals(FieldPieceConfig.RIGHT) && scaleTarget.equals(FieldPieceConfig.RIGHT)) {
			// at 1.0, reaches easy switch; at 2.0, reaches new cube; at 3.0, reaches easy scale
			final int[][] instructions = new int[][] {//TODO only takes care of stuff up until switch
				{0, 3, 270, 1},
				{3, Parameters.ElevatorPresets.SWITCH.height(), 270, 1},
				{1, Parameters.ElevatorPresets.SWITCH.height(), 270, 1}
			};
			
			
			final double[] triggers = new double[] {0.3, 0.7, 0.9};
			events = new V_Events(V_Events.getFromArray(instructions), triggers);

		}else if (switchTarget.equals(FieldPieceConfig.RIGHT)) {
			// at 1.0, reaches easy switch; at 2.0, reaches new cube; at 3.0, reaches hard scale
			final int[][] instructions = new int[][] {//TODO only takes care of stuff up until switch
				{0, 3, 270, 1},
				{3, Parameters.ElevatorPresets.SWITCH.height(), 270, 1},
				{1, Parameters.ElevatorPresets.SWITCH.height(), 270, 1}
			};
			
			
			final double[] triggers = new double[] {0.3, 0.7, 0.9};
			events = new V_Events(V_Events.getFromArray(instructions), triggers);

		}else if (scaleTarget.equals(FieldPieceConfig.RIGHT)) {
			// at 1.0, reaches easy scale; at 2.0, reaches new cube/hard switch
			final int[][] instructions = new int[][] {
				{4, 3, 0, 5},
				{3, 3, 0, 5},
				{4, Parameters.ElevatorPresets.SCALE_HIGH.height(), 0, 3},
				{4, Parameters.ElevatorPresets.SCALE_HIGH.height(), 0, 1},
				{1, Parameters.ElevatorPresets.SCALE_HIGH.height(), 0, 0}
//				{2, Parameters.ElevatorPresets.SCALE_HIGH.height(), 180, 1},
//				{0, Parameters.ElevatorPresets.SWITCH.height(), 180, 1},
//				{1, Parameters.ElevatorPresets.SWITCH.height(), 180, 1}
			};
			
			
			final double[] triggers = new double[] {0.1, 0.2, 0.6, 0.8, 1.0/*, 1.2, 1.6, 1.9*/};
			events = new V_Events(V_Events.getFromArray(instructions), triggers);

		}else {
			// at 1.0, almost to hard switch; at 2.0, reaches new cube/hard switch; at 3.0, reaches hard scale
			final int[][] instructions = new int[][] {//TODO only takes care of stuff up until switch
				{0, 3, 180, 1},
				{3, Parameters.ElevatorPresets.SWITCH.height(), 180, 1},
				{1, Parameters.ElevatorPresets.SWITCH.height(), 180, 1}
			};
			
			
			final double[] triggers = new double[] {0.3, 0.6, 1.9};
			events = new V_Events(V_Events.getFromArray(instructions), triggers);
		}
	}
	//------------------------------------------------------------------------------------------
	}
