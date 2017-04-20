package com.islavstan.talker.registration;



public interface RegistrationContract {
    interface RegistrationInteractor{
        void registration(String sex, String birthday);
    }
    interface RegistrationPresenter{
        void registration();
    }
}
