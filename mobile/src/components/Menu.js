import React, { Component } from 'react';
import { View, ScrollView, Text, TouchableOpacity, NetInfo } from 'react-native';
import { Actions, ActionConst } from 'react-native-router-flux';
import Icon from 'react-native-vector-icons/FontAwesome';
import PropTypes from 'prop-types';
import { connect } from 'react-redux';

import Collapsible from './Collapsible';
import { fetchReportTemplates, signoutUser } from '../actions';
import {
  CHW_READ_AUTHORITY, ASSIGN_MODULES_AUTHORITY, CHW_WRITE_AUTHORITY,
  DISPLAY_REPORTS_AUTHORITY, INCHARGE_READ_AUTHORITY, INCHARGE_WRITE_AUTHORITY,
  MANAGE_USERS_AUTHORITY, MANAGE_INCHARGE_USERS_AUTHORITY, hasAuthority,
} from '../utils/authorization';

const HIDE_NOT_IMPLEMENTED = true;

const styles = {
  container: {
    backgroundColor: '#FFF',
    flexDirection: 'column',
  },
  menuItem: {
    flexDirection: 'row',
    justifyContent: 'flex-start',
    alignItems: 'center',
    borderBottomColor: '#e7e7e7',
    borderBottomWidth: 1,
    height: 50,
  },
  menuItemText: {
    fontSize: 19,
    color: '#337ab7',
    paddingLeft: 5,
  },
  iconContainer: {
    justifyContent: 'center',
    alignItems: 'center',
    marginLeft: 10,
    width: 30,
  },
  title: {
    color: '#337ab7',
    fontSize: 36,
    marginTop: 20,
    marginBottom: 20,
    marginLeft: 20,
  },
};

class Menu extends Component {
  static contextTypes = {
    drawer: PropTypes.object,
  };
  constructor(props) {
    super(props);
    this.state = {
      CHW_READ_AUTHORITY: false,
      CHW_WRITE_AUTHORITY: false,
      INCHARGE_READ_AUTHORITY: false,
      INCHARGE_WRITE_AUTHORITY: false,
      ASSIGN_MODULES_AUTHORITY: false,
      DISPLAY_REPORTS_AUTHORITY: false,
      MANAGE_USERS_AUTHORITY: false,
      MANAGE_INCHARGE_USERS_AUTHORITY: false,
      currentScene: null,
      isConnected: false,
    };
    this.handleConnectivityChange = this.handleConnectivityChange.bind(this);
  }

  componentWillMount() {
    hasAuthority(CHW_READ_AUTHORITY).then((result) => {
      if (result) { this.setState({ CHW_READ_AUTHORITY: true }); }
    });
    hasAuthority(CHW_WRITE_AUTHORITY).then((result) => {
      if (result) { this.setState({ CHW_WRITE_AUTHORITY: true }); }
    });
    hasAuthority(INCHARGE_READ_AUTHORITY).then((result) => {
      if (result) { this.setState({ INCHARGE_READ_AUTHORITY: true }); }
    });
    hasAuthority(INCHARGE_WRITE_AUTHORITY).then((result) => {
      if (result) { this.setState({ INCHARGE_WRITE_AUTHORITY: true }); }
    });
    hasAuthority(ASSIGN_MODULES_AUTHORITY).then((result) => {
      if (result) { this.setState({ ASSIGN_MODULES_AUTHORITY: true }); }
    });
    hasAuthority(DISPLAY_REPORTS_AUTHORITY).then((result) => {
      if (result) {
        this.setState({ DISPLAY_REPORTS_AUTHORITY: true });
        NetInfo.isConnected.fetch().then((isConnected) => {
          if (isConnected) {
            this.props.fetchReportTemplates();
          }
        });
      }
    });
    hasAuthority(MANAGE_USERS_AUTHORITY).then((result) => {
      if (result) { this.setState({ MANAGE_USERS_AUTHORITY: true }); }
    });
    hasAuthority(MANAGE_INCHARGE_USERS_AUTHORITY).then((result) => {
      if (result) { this.setState({ MANAGE_INCHARGE_USERS_AUTHORITY: true }); }
    });
    NetInfo.getConnectionInfo().then(this.handleConnectivityChange);
    NetInfo.addEventListener(
      'connectionChange',
      this.handleConnectivityChange,
    );
  }

  componentWillUnmount() {
    NetInfo.removeEventListener(
      'connectionChange',
      this.handleConnectivityChange,
    );
  }

  handleConnectivityChange(connectionInfo) {
    const { type } = connectionInfo;
    const isConnected = (type !== 'none' && type !== 'unknown');
    this.setState({
      isConnected,
    });
  }

  openSection(sectionKey) {
    Actions[sectionKey].call();
    this.context.drawer.close();
    this.setState({ currentScene: sectionKey });
  }

  openReportSection(props) {
    if (this.state.currentScene === 'report') {
      Actions.refresh(props);
    } else {
      Actions.report(props);
      this.setState({ currentScene: 'report' });
    }
    this.context.drawer.close();
  }

  logout() {
    this.props.signoutUser();
    Actions.auth({ type: ActionConst.RESET });
    this.setState({ currentScene: 'auth' });
  }

  renderReports() {
    if (this.props.reportTemplates) {
      return (
        <View>
          {
            this.props.reportTemplates.map(report => (
              <TouchableOpacity
                onPress={() => this.openReportSection({
                  reportName: report.name,
                  reportId: report.id,
                  templateParameters: report.templateParameters,
                })}
                style={styles.menuItem}
                key={report.id}
              >
                <View style={[styles.iconContainer, { marginLeft: 15 }]}>
                  <Icon name="file-text-o" size={14} color="#337ab7" />
                </View>
                <Text style={[styles.menuItemText, { fontSize: 14 }]}>{report.name.replace(' Report', '')}
                </Text>
              </TouchableOpacity>
            ))
          }
        </View>
      );
    }
    return (
      <View />
    );
  }

  render() {
    return (
      <View style={{ flex: 1 }}>
        <Text style={styles.title}>Menu</Text>
        <ScrollView style={styles.container}>
          {this.state.isConnected &&
          <TouchableOpacity
            onPress={() => this.openSection('profile')}
            style={styles.menuItem}
          >
            <View style={styles.iconContainer}>
              <Icon name="user" size={20} color="#337ab7" />
            </View>
            <Text style={styles.menuItemText}>Profile</Text>
          </TouchableOpacity>
          }

          <TouchableOpacity
            onPress={() => this.openSection('home')}
            style={styles.menuItem}
          >
            <View style={styles.iconContainer}>
              <Icon name="home" size={20} color="#337ab7" />
            </View>
            <Text style={styles.menuItemText}>Home</Text>
          </TouchableOpacity>

          { this.state.isConnected
          && (this.state.CHW_READ_AUTHORITY || this.state.CHW_WRITE_AUTHORITY) &&
          <Collapsible title="CHW" headerIcon="users" style={styles.menuItem}>
            <View>
              { this.state.CHW_WRITE_AUTHORITY &&
              <TouchableOpacity
                onPress={() => this.openSection('chwsNew')}
                style={styles.menuItem}
              >
                <View style={[styles.iconContainer, { marginLeft: 30 }]}>
                  <Icon name="plus" size={20} color="#337ab7" />
                </View>
                <Text style={styles.menuItemText}>Add CHW</Text>
              </TouchableOpacity>
              }
              { this.state.CHW_READ_AUTHORITY &&
              <View>
                <TouchableOpacity
                  onPress={() => this.openSection('allChws')}
                  style={styles.menuItem}
                >
                  <View style={[styles.iconContainer, { marginLeft: 30 }]}>
                    <Icon name="list" size={20} color="#337ab7" />
                  </View>
                  <Text style={styles.menuItemText}>CHW List</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  onPress={() => this.openSection('chws')}
                  style={styles.menuItem}
                >
                  <View style={[styles.iconContainer, { marginLeft: 30 }]}>
                    <Icon name="list" size={20} color="#337ab7" />
                  </View>
                  <Text style={styles.menuItemText}>Selected CHW List</Text>
                </TouchableOpacity>
              </View>
              }
            </View>
          </Collapsible>
          }

          { this.state.isConnected && this.state.ASSIGN_MODULES_AUTHORITY &&
          <Collapsible title="Modules" headerIcon="graduation-cap" style={styles.menuItem}>
            <TouchableOpacity
              onPress={() => this.openSection('modulesToDistrict')}
              style={styles.menuItem}
            >
              <View style={[styles.iconContainer, { marginLeft: 30 }]}>
                <Icon name="check" size={20} color="#337ab7" />
              </View>
              <Text style={styles.menuItemText}>Assign</Text>
            </TouchableOpacity>
          </Collapsible>
          }

          { this.state.isConnected
          && (this.state.INCHARGE_WRITE_AUTHORITY || this.state.INCHARGE_READ_AUTHORITY) &&
          <Collapsible title="Incharge" headerIcon="user-md" style={styles.menuItem}>
            <View>
              { this.state.INCHARGE_WRITE_AUTHORITY &&
              <TouchableOpacity
                onPress={() => this.openSection('inchargesNew')}
                style={styles.menuItem}
              >
                <View style={[styles.iconContainer, { marginLeft: 30 }]}>
                  <Icon name="plus" size={20} color="#337ab7" />
                </View>
                <Text style={styles.menuItemText}>Add Incharge</Text>
              </TouchableOpacity>
              }

              { this.state.isConnected && this.state.INCHARGE_READ_AUTHORITY &&
              <View>
                <TouchableOpacity
                  onPress={() => this.openSection('allIncharges')}
                  style={styles.menuItem}
                >
                  <View style={[styles.iconContainer, { marginLeft: 30 }]}>
                    <Icon name="list" size={20} color="#337ab7" />
                  </View>
                  <Text style={styles.menuItemText}>Incharge List</Text>
                </TouchableOpacity>
                <TouchableOpacity
                  onPress={() => this.openSection('incharges')}
                  style={styles.menuItem}
                >
                  <View style={[styles.iconContainer, { marginLeft: 30 }]}>
                    <Icon name="list" size={20} color="#337ab7" />
                  </View>
                  <Text style={styles.menuItemText}>Selected Incharge List</Text>
                </TouchableOpacity>
              </View>
              }
            </View>
          </Collapsible>
          }

          { this.state.DISPLAY_REPORTS_AUTHORITY &&
          <Collapsible title="Reports" headerIcon="bar-chart" style={styles.menuItem}>
            {this.renderReports()}
          </Collapsible>
          }

          { this.state.isConnected
          && (this.state.MANAGE_USERS_AUTHORITY || this.state.MANAGE_INCHARGE_USERS_AUTHORITY) &&
            <Collapsible title="Users" headerIcon="user" style={styles.menuItem}>
              <View>
                <TouchableOpacity
                  onPress={() => this.openSection('userNew')}
                  style={styles.menuItem}
                >
                  <View style={[styles.iconContainer, { marginLeft: 30 }]}>
                    <Icon name="plus" size={20} color="#337ab7" />
                  </View>
                  <Text style={styles.menuItemText}>Add User</Text>
                </TouchableOpacity>

                <TouchableOpacity
                  onPress={() => this.openSection('users')}
                  style={styles.menuItem}
                >
                  <View style={[styles.iconContainer, { marginLeft: 30 }]}>
                    <Icon name="list" size={20} color="#337ab7" />
                  </View>
                  <Text style={styles.menuItemText}>User List</Text>
                </TouchableOpacity>
              </View>
            </Collapsible>
          }

          { !HIDE_NOT_IMPLEMENTED &&
          <TouchableOpacity
            onPress={() => this.openSection('synchronizeView')}
            style={styles.menuItem}
          >
            <View style={styles.iconContainer}>
              <Icon name="refresh" size={20} color="#337ab7" />
            </View>
            <Text style={styles.menuItemText}>Synchronize</Text>
          </TouchableOpacity>
          }

          <TouchableOpacity
            onPress={() => this.logout()}
            style={styles.menuItem}
          >
            <View style={styles.iconContainer}>
              <Icon name="sign-out" size={20} color="#337ab7" />
            </View>
            <Text style={styles.menuItemText}>Logout</Text>
          </TouchableOpacity>

        </ScrollView>
      </View>
    );
  }
}

function mapStateToProps(state) {
  return {
    reportTemplates: state.reportReducer.templates,
  };
}

export default connect(mapStateToProps, { signoutUser, fetchReportTemplates })(Menu);

Menu.propTypes = {
  reportTemplates: PropTypes.arrayOf(PropTypes.shape({})).isRequired,
  signoutUser: PropTypes.func.isRequired,
  fetchReportTemplates: PropTypes.func.isRequired,
};
