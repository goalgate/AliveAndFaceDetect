package cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.module;

import android.hardware.Camera;

import cn.cbsd.aliveandfacedetect.Func.Func_Camera.mvp.presenter.PhotoPresenter;

public class FaceConfig {

    public static boolean isBinocular = true;

    public static PhotoPresenter.EquipmentType equipmentType = PhotoPresenter.EquipmentType.Custom_machine;

    public static int FaceDetectCamera = Camera.CameraInfo.CAMERA_FACING_BACK;

    public static int ShowingCamera = Camera.CameraInfo.CAMERA_FACING_FRONT;

    public static IPhotoModule camera_module() {
        if(equipmentType.equals(PhotoPresenter.EquipmentType.Custom_machine)){
            return new Custom_MachinePhotoModuleImpl();
        }else{
            return new PhonePhotoModuleImpl();
            
        }
    }

}
