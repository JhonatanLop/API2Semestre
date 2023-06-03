package org.openjfx.api2semestre.view.utils.dashboard;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openjfx.api2semestre.appointment.Appointment;
import org.openjfx.api2semestre.authentication.Permission;
import org.openjfx.api2semestre.authentication.Profile;
import org.openjfx.api2semestre.authentication.User;
import org.openjfx.api2semestre.data.HasDisplayName;
import org.openjfx.api2semestre.data.ResultCenter;
import org.openjfx.api2semestre.database.QueryLibs;

public enum DashboardContext implements HasDisplayName {

    MyContext      (
        Profile.Colaborador,
        Permission.Appoint,
        new FilterField[0]
    ),
    ManagedContext (
        Profile.Gestor,
        Permission.Validate,
        new FilterField[] { FilterField.Requester }
    ),
    FullContext    (
        Profile.Administrator,
        Permission.FullAccess,
        new FilterField[] { FilterField.Requester, FilterField.Manager }
    );

    private Profile profile;
    private Permission permission;
    private FilterField[] fields;

    private DashboardContext (Profile profile, Permission permission, FilterField[] fields) {
        final FilterField[] BASE = new FilterField[] {
            FilterField.AppointmentStart,
            FilterField.AppointmentEnd,
            FilterField.AppointmentType,
            FilterField.ResultCenter,
            FilterField.Project,
            FilterField.Client,
        };

        this.profile = profile;
        this.permission = permission;
        this.fields = new FilterField[BASE.length + fields.length];

        for (int i = 0; i < this.fields.length; i++) {
            if (i < BASE.length) {
                this.fields[i] = BASE[i];
            } else {
                this.fields[i] = fields[i - BASE.length];
            }
        }
    }

    public boolean userHasAccess (Permission[] permissions) {
        for (Permission userPermission : permissions) {
            if (userPermission != this.permission) continue;
            return true;
        }
        return false;
    }


    public Appointment[] loadData(User currentUser) {
        switch (profile) {
            case Administrator: return QueryLibs.selectAllAppointments();
            case Colaborador: return QueryLibs.selectAppointmentsByUser(currentUser.getId());
            case Gestor:

                ResultCenter[] resultCentersManagedBy = QueryLibs.selectResultCentersManagedBy(currentUser.getId());
                if (resultCentersManagedBy.length > 0) {
                    List<Appointment> appointmentsOfCRsManagedBy = new ArrayList<Appointment>();
                    for (ResultCenter resultCenter : resultCentersManagedBy) {
                    appointmentsOfCRsManagedBy.addAll(Arrays.asList(QueryLibs.selectAppointmentsOfResultCenter(resultCenter.getId())));
                    }
                    return appointmentsOfCRsManagedBy.toArray(Appointment[]::new);
                }
            
            default: return new Appointment[0];
        }
    }

    public Profile getProfile() { return profile; }
    public FilterField[] getFields() { return fields; }
    @Override public String getName() { return profile.getName(); }

}
