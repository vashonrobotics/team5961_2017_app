package org.firstinspires.ftc.teamcode;

import android.graphics.Color;

import com.qualcomm.robotcore.eventloop.opmode.Autonomous;
import com.qualcomm.robotcore.eventloop.opmode.LinearOpMode;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.Servo;

import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.matrices.OpenGLMatrix;
import org.firstinspires.ftc.robotcore.external.matrices.VectorF;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.RelicRecoveryVuMark;
import org.firstinspires.ftc.robotcore.external.navigation.VuMarkInstanceId;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaLocalizer;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackable;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackableDefaultListener;
import org.firstinspires.ftc.robotcore.external.navigation.VuforiaTrackables;

import java.util.ArrayList;


/**
 * Created by FTC on 9/23/2017.
 * updated by Caz
 */
@Autonomous(name = "Autonomous base", group = "Vashon 5961")
public class AutonomousModeBase extends LinearOpMode {
    private boolean isRed = true;
    private ArrayList baseMotorArray = new ArrayList();
    private VuforiaLocalizer vuforia;
    OpenGLMatrix lastLocation = null;
    private static final String TAG = "Vuforia Navigation Sample";
    private ColorSensor jewelColor;
    double wheelCircumference = 100.0 * Math.PI; // circumference in mm
    double ticksPerRotation = 1125.0;  // number of encoder ticks to make a full rotation (about)
    VuforiaLocalizer.Parameters parameters;
    VuforiaTrackable relicTemplate;
    Servo leftServo;
    Servo rightServo;
    Servo jewelMover;
    Servo jewelExtender;

//    public AutonomousModeBase(boolean isRed){
//        this.isRed = isRed;
//    }

    @Override
    public void runOpMode() throws InterruptedException {

        // get wheels from config

        baseMotorArray.add(hardwareMap.dcMotor.get("front Left"));
        baseMotorArray.add(hardwareMap.dcMotor.get("front Right"));
        baseMotorArray.add(hardwareMap.dcMotor.get("back Left"));
        baseMotorArray.add(hardwareMap.dcMotor.get("back Right"));
        ((DcMotor) baseMotorArray.get(1)).setDirection(DcMotor.Direction.REVERSE);
        ((DcMotor) baseMotorArray.get(3)).setDirection(DcMotor.Direction.REVERSE);

        leftServo = hardwareMap.servo.get("left");
        rightServo = hardwareMap.servo.get("right");
        leftServo.setDirection(Servo.Direction.REVERSE);
        leftServo.setPosition(0.8);
        rightServo.setPosition(0.8);
//        jewelMover = hardwareMap.servo.get("jewelMover");
//        jewelExtender = hardwareMap.servo.get("jewelExtender");
////        jewelColor = hardwareMap.colorSensor.get("jewelColor");


        parameters = new VuforiaLocalizer.Parameters();

        parameters.vuforiaLicenseKey = "";
        parameters.cameraDirection = VuforiaLocalizer.CameraDirection.FRONT;

        this.vuforia = ClassFactory.createVuforiaLocalizer(parameters);

        final VuforiaTrackables relicTrackables = this.vuforia.loadTrackablesFromAsset("RelicVuMark");
        relicTemplate = relicTrackables.get(0);
        relicTemplate.setName("relicVuMarkTemplate"); // can help in debugging; otherwise not necessary

        relicTrackables.activate();
        /*
         * We also indicate which camera on the RC that we wish to use.
         * Here we chose the back (HiRes) camera (for greater range), but
         * for a competition robot, the front camera might be more convenient.
         */


        // need to set servo pos when working


        waitForStart();

//        lookForJewel();
        final KeyPositions keyColumnPos = moveToFindPictograph();
//        alignWithPictograph(false);
//        alignWithPictograph(true);
        alignWithPictograph2(true);
//sleep(10000);
        goBackToCryptoBox(keyColumnPos);
       TurnAroundAndGoAwayFromCryptoBoxAndBack(keyColumnPos);

        goIntoCryptoBox();
        letGoOfGlyph();
        moveAwayFromGlyph();


//        telemetry.update();
//        RobotPos();
        requestOpModeStop();
    }
    private void lookForJewel() {
        int numberOfReds = 0;
        int numberOfBlues = 0;
        for(int i = 0; i < 4; i++) {
            float[] colorInHSV = {};
            Color.RGBToHSV(jewelColor.red(), jewelColor.green(), jewelColor.blue(), colorInHSV);

            float hue = colorInHSV[0];
            if ((hue >= 330) && (hue != 0)) {
                numberOfReds++;
            } else if (120 <= hue && hue <= 250) {
                numberOfBlues++;
            }
            sleep(100);
        }
        if (numberOfBlues >= 2 && numberOfReds <= 2) { // blue
            jewelMover.setPosition(1.0);
        } else if (numberOfReds >= 2 && numberOfBlues <= 2) { // red
            jewelMover.setPosition(0.0);
        }


        sleep(200);

    }
    private void goTowardCryptoBoxPartWay() {
        DriveTrain.mecanum(baseMotorArray, 0.0, -1.0, 0.0);
//        sleep(750);
        sleepUntilEncodersChangeToACertainValue(2900, 1000);
        DriveTrain.mecanum(baseMotorArray, 0.0, 0.0, 0.0);
        sleep(200);
    }

    private void moveAwayFromGlyph() {
        DriveTrain.mecanum(baseMotorArray, 0.0, 0.7, 0.0);
        sleep(200);
        DriveTrain.mecanum(baseMotorArray, 0.0, 0.0, 0.0);
        sleep(200);
        DriveTrain.mecanum(baseMotorArray, 0.0, -1.0, 0.0);
        sleep(300);
        DriveTrain.mecanum(baseMotorArray, 0.0, 0.0, 0.0);
        sleep(200);
        DriveTrain.mecanum(baseMotorArray, 0.0, 1.0, 0.0);
        sleep(300);
        DriveTrain.mecanum(baseMotorArray, 0.0, 0.0, 0.0);
    }

    private void TurnAroundAndGoAwayFromCryptoBoxAndBack(KeyPositions keyPosition) {
        DriveTrain.mecanum(baseMotorArray, 1.0, 0.0, 0.0);
//        sleep(500);
        sleepUntilEncodersChangeToACertainValue(1533, 1000);
        if (keyPosition == KeyPositions.Center || keyPosition == KeyPositions.Unknown){
            //sleep(200);
            sleepUntilEncodersChangeToACertainValue(533, 400);

        }
        if (keyPosition == KeyPositions.Left){
            sleepUntilEncodersChangeToACertainValue(733, 500);
//            sleep(300);
        }
        DriveTrain.mecanum(baseMotorArray, 0.0, 0.0, 0.0);
        sleep(2000);
        alignWithPictograph2(true);
        DriveTrain.mecanum(baseMotorArray, 0.0, 0.0, 1.0);
        double startOfTurn = ((DcMotor)baseMotorArray.get(0)).getCurrentPosition();
        final long minTime = System.currentTimeMillis()+1000;
//        while(((DcMotor)baseMotorArray.get(0)).getCurrentPosition() < startOfTurn + 3700 || (System.currentTimeMillis() < minTime)){
//            sleep(10);
//        }
        sleepUntilEncodersChangeToACertainValue(3700, 1800);
        while(System.currentTimeMillis() < minTime) {
            sleep(10);
        }
        //sleep(1490);
        DriveTrain.mecanum(baseMotorArray, 0.0, 0.0, 0.0);
        sleep(1000);
        goTowardCryptoBoxPartWay();
        DriveTrain.mecanum(baseMotorArray, 1.0, 0.0, 0.0);
//        sleep(500);
        sleepUntilEncodersChangeToACertainValue(1533, 1000);
        if (keyPosition == KeyPositions.Center || keyPosition == KeyPositions.Unknown){
            //sleep(200);
            sleepUntilEncodersChangeToACertainValue(533, 400);

        }
        if (keyPosition == KeyPositions.Left){
            sleepUntilEncodersChangeToACertainValue(733, 500);
//            sleep(300);
        }
        DriveTrain.mecanum(baseMotorArray, 0.0, 0.0, 0.0);
        sleep(500);
    }


    private void alignWithPictograph(boolean fixingTurn) {// needs to be tested
        final long startTime = System.currentTimeMillis();
        final long maxTime = 1000 + startTime;
        double valueToDecrease;
        if (fixingTurn){
            valueToDecrease = findPictograph().rotation;
            telemetry.addData("rotation: ",valueToDecrease);
        }else {
            valueToDecrease = findPictograph().horizontalOffSet;
            telemetry.addData("horizontal off set: ",valueToDecrease);
        }


        boolean withinRange = Math.abs(valueToDecrease) < 5.0;

        if (fixingTurn){
            if (findPictograph().rotation > 0.0){
                for (int i = 0; i < 4; i += 2){
                    ((DcMotor)baseMotorArray.get(i)).setPower(0.4);
                }
            }else {
                for (int i = 0; i < 4; i += 2){
                    ((DcMotor)baseMotorArray.get(i)).setPower(0.4);
                }
            }
        }else {
            if (findPictograph().horizontalOffSet > 0.0){
                DriveTrain.mecanum(baseMotorArray,0.0,0.5,0.0);
            }else {
                DriveTrain.mecanum(baseMotorArray,0.0,-0.5,0.0);
            }
        }

        while (!withinRange && System.currentTimeMillis() < maxTime){

            if (fixingTurn){
                valueToDecrease = findPictograph().rotation;
                telemetry.addData("rotation: ",valueToDecrease);
            }else {
                valueToDecrease = findPictograph().horizontalOffSet;
                telemetry.addData("horizontal off set: ",valueToDecrease);
            }
            telemetry.update();
            withinRange = Math.abs(valueToDecrease) < 5.0;
            sleep(10);
        }
        DriveTrain.mecanum(baseMotorArray, 0.0, 0.0, 0.0);
        sleep(200);
    }
    private void alignWithPictograph2(boolean fixingTurn) {
        sleep(300);
        double rotation = findPictograph().rotation;
        if (rotation != 0) {
            for (int i = 0; i < 4; i++) {


                int directionOfTurn = 1;
                if (i % 2 == 0) {
                    directionOfTurn *= -1;
                }
                DcMotor motor = ((DcMotor) baseMotorArray.get(i));
                int distanceForTurn = ((int)(3700 / (180 / rotation)))*directionOfTurn;

//
                motor.setMode(DcMotor.RunMode.RUN_TO_POSITION);

//                if (i % 2 == 0){
//                    directionOfTurn *= -1;
//                    telemetry.addData("direction ", directionOfTurn);
//                }
//                if (distanceForTurn > 0 ) {
//                    directionOfTurn *= -1;
//                    if (i % 2 == 0){
//                        ((DcMotor) baseMotorArray.get(i)).setPower(-0.7);
//                    } else{
//
//                        ((DcMotor) baseMotorArray.get(i)).setPower(0.7);
//                    }
//                } //else{
//                    if (i % 2 == 0){
//                        ((DcMotor) baseMotorArray.get(i)).setPower(0.7);
//                    } else{
//
//                        ((DcMotor) baseMotorArray.get(i)).setPower(-0.7);
//                    }
//                }
                motor.setTargetPosition(motor.getCurrentPosition() + distanceForTurn);
                motor.setPower(1);


                telemetry.addData("Distance to Turn:", distanceForTurn);
                telemetry.addData("motor powers", motor);

            }
            // if this doesn't work

//            while(distanceToTurn > ((DcMotor)baseMotorArray.get(0)).getCurrentPosition() ) {
//                sleep(10);
//            }


            sleep(500);
            telemetry.update();
            for (int i = 0; i < 4; i++) {
                ((DcMotor)baseMotorArray.get(i)).setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                ((DcMotor)baseMotorArray.get(i)).setPower(0);
            }
            sleep(500);
        }
    }


    private KeyPositions moveToFindPictograph() {
        DriveTrain.mecanum(baseMotorArray,0.0,0.5,0.0);

        // move robot to pictograph
        final long startTime = System.currentTimeMillis();
        final long maxTime = 300 + startTime;
        boolean found = false;
        telemetry.addData("begin loop at: ", System.currentTimeMillis());
        telemetry.addData("time limit: ", maxTime);
        while(!found && System.currentTimeMillis() < maxTime)

        {
            // Read pictograph
            DecodedPictographInfo pictographInfo = findPictograph();
            found = pictographInfo.keyPosition != KeyPositions.Unknown;
            sleep(10);
        }
        telemetry.addData("end loop at: ", System.currentTimeMillis());
        telemetry.update();
        DriveTrain.mecanum(baseMotorArray,0.0,0.0,0.0);
        sleep(2000);
        KeyPositions keyColumnPos = findPictograph().keyPosition;
        for(int i = -1; i < 2; i += 2) { // search twice more for the pictograph if can't find it
            if (keyColumnPos == KeyPositions.Unknown) {
                DriveTrain.mecanum(baseMotorArray, 0.0, 0.6*i, 0.0);
                sleep(100);
                if (i > 0){
                    sleep(100);
                }
                DriveTrain.mecanum(baseMotorArray, 0.0, 0.0, 0.0);
                sleep(2000);
                keyColumnPos = findPictograph().keyPosition;
            }
        }
        telemetry.addData("key pos: ",keyColumnPos);
        telemetry.update();

        return keyColumnPos;
    }


    private DecodedPictographInfo findPictograph(){
        KeyPositions keyColumnPos = KeyPositions.Unknown;
        double tZ = 0.0;
        double tX = 0.0;
        double rY = 0.0;

        /**
         * {@link #vuforia} is the variable we will use to store our instance of the Vuforia
         * localization engine.
         */


        /*
         * To start up Vuforia, tell it the view that we wish to use for camera monitor (on the RC phone);
         * If no camera monitor is desired, use the parameterless constructor instead (commented out below).
         */


        // OR...  Do Not Activate the Camera Monitor View, to save power
        // VuforiaLocalizer.Parameters parameters = new VuforiaLocalizer.Parameters();

        /*
         * IMPORTANT: You need to obtain your own license key to use Vuforia. The string below with which
         * 'parameters.vuforiaLicenseKey' is initialized is for illustration only, and will not function.
         * A Vuforia 'Development' license key, can be obtained free of charge from the Vuforia developer
         * web site at https://developer.vuforia.com/license-manager.
         *
         * Vuforia license keys are always 380 characters long, and look as if they contain mostly
         * random data. As an example, here is a example of a fragment of a valid key:
         *      ... yIgIzTqZ4mWjk9wd3cZO9T1axEqzuhxoGlfOOI2dRzKS4T0hQ8kT ...
         * Once you've obtained a license key, copy the string from the Vuforia web site
         * and paste it in to your code onthe next line, between the double quotes.
         */


        /**
         * Load the data set containing the VuMarks for Relic Recovery. There's only one trackable
         * in this data set: all three of the VuMarks in the game were created from this one template,
         * but differ in their instance id information.
         * @see VuMarkInstanceId
         */



            /**
             * See if any of the instances of {@link relicTemplate} are currently visible.
             * {@link RelicRecoveryVuMark} is an enum which can have the following values:
             * UNKNOWN, LEFT, CENTER, and RIGHT. When a VuMark is visible, something other than
             * UNKNOWN will be returned by {@link RelicRecoveryVuMark#from(VuforiaTrackable)}.
             */
            RelicRecoveryVuMark vuMark = RelicRecoveryVuMark.from(relicTemplate);


                /* Found an instance of the template. In the actual game, you will probably
                 * loop until this condition occurs, then move on to act accordingly depending
                 * on which VuMark was visible. */
                telemetry.addData("VuMark", "%s visible", vuMark);
                switch (vuMark){

                    case UNKNOWN:
                        keyColumnPos = KeyPositions.Unknown;
                        break;
                    case LEFT:
                        keyColumnPos = KeyPositions.Left;
                        break;
                    case CENTER:
                        keyColumnPos = KeyPositions.Center;
                        break;
                    case RIGHT:
                        keyColumnPos = KeyPositions.Right;

                }
                /* For fun, we also exhibit the navigational pose. In the Relic Recovery game,
                 * it is perhaps unlikely that you will actually need to act on this pose information, but
                 * we illustrate it nevertheless, for completeness. */
                OpenGLMatrix pose = ((VuforiaTrackableDefaultListener) relicTemplate.getListener()).getPose();
                telemetry.addData("Pose", format(pose));

                /* We further illustrate how to decompose the pose into useful rotational and
                 * translational components */
                if (pose != null && keyColumnPos != KeyPositions.Unknown) {
                    VectorF trans = pose.getTranslation();
                    Orientation rot = Orientation.getOrientation(pose, AxesReference.EXTRINSIC, AxesOrder.XYZ, AngleUnit.DEGREES);

                    // Extract the X, Y, and Z components of the offset of the target relative to the robot
                    tX = trans.get(0);
                    double tY = trans.get(1);
                    tZ = trans.get(2);

                    // Extract the rotational components of the target relative to the robot
                    double rX = rot.firstAngle;
                    rY = rot.secondAngle;
                    double rZ = rot.thirdAngle;
                }


            telemetry.update();

        return new DecodedPictographInfo(tZ, tX, keyColumnPos, rY);
    }
    private enum KeyPositions {
        Left,Right,Center,Unknown;
    }
    private void mecanumDriveForDistance(Double angle, Double power, Double distance){
        double radians = (angle * Math.PI) / 180.0;
        double x = Math.cos(radians) * power;
        double y = Math.sin(radians) * power;
        DriveTrain.mecanum(baseMotorArray, x, y, 0.0);
        int startPos = ((DcMotor)baseMotorArray.get(1)).getCurrentPosition();
        while (Math.abs(((DcMotor) baseMotorArray.get(1)).getCurrentPosition()) < ((int)((distance / wheelCircumference) * ticksPerRotation) + startPos)){
            sleep(10);
        }
    }
    private String format(OpenGLMatrix transformationMatrix) {
        return (transformationMatrix != null) ? transformationMatrix.formatAsTransform() : "null";
    }
    private class DecodedPictographInfo {
        public final double distance;
        public final double horizontalOffSet;
        public final KeyPositions keyPosition;
        public final double rotation;

        public DecodedPictographInfo(double distanceFromPictograph, double horizontalDistance, KeyPositions keyCollumn, double yRotation) {
            distance = distanceFromPictograph;
            horizontalOffSet = horizontalDistance;
            keyPosition = keyCollumn;
            rotation = yRotation;
        }



    }
    void goBackToCryptoBox(KeyPositions keyColumnPos) {
        DriveTrain.mecanum(baseMotorArray, 0.5, 0.0, 0.0);
        long startTimeForBackingUp = System.currentTimeMillis();
        switch (keyColumnPos) {
            case Right:
                while (findPictograph().distance > -280.0 && startTimeForBackingUp < (700 + startTimeForBackingUp)) {
                    if (findPictograph().distance == 0){
                        telemetry.addData("LostPictographPos: ", findPictograph().distance);

                    }
                    sleep(10);
                    telemetry.addData("Dist:", findPictograph().distance);
                    telemetry.update();
                }
                break;
            case Unknown:
                sleep(700);
                break;

            case Center:
                while (findPictograph().distance > -540.0 && startTimeForBackingUp < (1000 + startTimeForBackingUp)) {
                    if (findPictograph().distance == 0){
                        telemetry.addData("LostPictographPos: ", findPictograph().distance);
                        sleep(2000);
                    }
                    sleep(10);
                    telemetry.addData("Dist:", findPictograph().distance);
                    telemetry.update();
                }

                break;
            case Left:
                while (findPictograph().distance > -800.0 && startTimeForBackingUp < (2000 + startTimeForBackingUp)) {
                    sleep(10);
                    if (findPictograph().distance == 0){
                        telemetry.addData("LostPictographPos: ", findPictograph().distance);
                    }
                    telemetry.addData("Dist:", findPictograph().distance);
                    telemetry.update();
                }
                break;
        }
        DriveTrain.mecanum(baseMotorArray,0.0,0.0,0.0);
        sleep(400);
    }
    private void goIntoCryptoBox() {
        DriveTrain.mecanum(baseMotorArray, 0.0, -0.7, 0.0);
        sleep(1000);
        DriveTrain.mecanum(baseMotorArray, 0.0, 0.0, 0.0);
    }
    private void letGoOfGlyph() {
        leftServo.setPosition(0.9);
        rightServo.setPosition(0.8);
        sleep(500);
    }
    private void sleepUntilEncodersChangeToACertainValue(int value, long maxSleepTime) {
        int[] startPositions = {0,0,0,0};
        for (int i = 0; i < 4; i++) {
            startPositions[i] = ((DcMotor) baseMotorArray.get(i)).getCurrentPosition();
        }
//        int[] changePerMotor = {0,0,0,0};
        boolean isReady = false;
        long maxTime = System.currentTimeMillis() + maxSleepTime;
        while(!isReady && System.currentTimeMillis() < maxTime) {
            sleep(10);
            boolean isPartiallyReady = false;
            for (int i = 0; i < 4; i++) {
                int changePerMotor = Math.abs(((DcMotor) baseMotorArray.get(i)).getCurrentPosition() - startPositions[i]);
                if (Math.abs(changePerMotor - value) <= 100) {
                    if (!isPartiallyReady) {
                        isPartiallyReady = true;
                    } else {
                        isReady = true;
                    }
                }
            }
        }
        if (System.currentTimeMillis() >= maxTime){
            DriveTrain.mecanum(baseMotorArray, 0.0, 0.0, 0.0);
            telemetry.addData("quit because it took to long: ",System.currentTimeMillis() >= maxTime);
            telemetry.update();
            sleep(10000);
        }
    }
}
