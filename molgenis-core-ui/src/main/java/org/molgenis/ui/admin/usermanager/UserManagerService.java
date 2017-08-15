package org.molgenis.ui.admin.usermanager;

import java.util.List;
import org.molgenis.auth.Group;

public interface UserManagerService {
  List<UserViewData> getAllUsers();

  void setActivationUser(String userId, Boolean active);

  void setActivationGroup(String groupId, Boolean active);

  List<Group> getAllGroups();

  List<Group> getGroupsWhereUserIsMember(String userId);

  List<Group> getGroupsWhereUserIsNotMember(String userId);

  List<UserViewData> getUsersMemberInGroup(String groupId);

  void addUserToGroup(String molgenisGroupId, String molgenisUserId);

  void removeUserFromGroup(String molgenisGroupId, String molgenisUserId);
}
