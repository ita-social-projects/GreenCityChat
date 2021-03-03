package greencity.entity;

import greencity.enums.UserStatus;
import javax.annotation.Generated;
import javax.persistence.metamodel.ListAttribute;
import javax.persistence.metamodel.SingularAttribute;
import javax.persistence.metamodel.StaticMetamodel;

@Generated(value = "org.hibernate.jpamodelgen.JPAMetaModelEntityProcessor")
@StaticMetamodel(Participant.class)
public abstract class Participant_ {

	public static volatile SingularAttribute<Participant, String> profilePicture;
	public static volatile ListAttribute<Participant, ChatRoom> rooms;
	public static volatile SingularAttribute<Participant, UserStatus> userStatus;
	public static volatile SingularAttribute<Participant, String> name;
	public static volatile SingularAttribute<Participant, Long> id;
	public static volatile SingularAttribute<Participant, String> email;

	public static final String PROFILE_PICTURE = "profilePicture";
	public static final String ROOMS = "rooms";
	public static final String USER_STATUS = "userStatus";
	public static final String NAME = "name";
	public static final String ID = "id";
	public static final String EMAIL = "email";

}

